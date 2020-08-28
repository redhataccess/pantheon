package com.redhat.pantheon.asciidoctor;

import com.redhat.pantheon.asciidoctor.extension.HtmlModulePostprocessor;
import com.redhat.pantheon.asciidoctor.extension.MetadataExtractorTreeProcessor;
import com.redhat.pantheon.asciidoctor.extension.PantheonIncludeProcessor;
import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import com.redhat.pantheon.asciidoctor.extension.XrefPreprocessor;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.HashableFileResource;
import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.api.util.ResourceTraversal;
import com.redhat.pantheon.model.assembly.Assembly;
import com.redhat.pantheon.model.assembly.AssemblyVersion;
import com.redhat.pantheon.model.assembly.TableOfContents;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentLocale;
import com.redhat.pantheon.model.document.DocumentMetadata;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.document.DocumentVersion;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.model.workspace.ModuleVariantDefinition;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.redhat.pantheon.helper.PantheonConstants.MACRO_INCLUDE;
import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;
import static java.util.stream.Collectors.toMap;

/**
 * Business service class which provides Asciidoctor-related methods which work in conjunction with other
 * business entities such as model classes.
 */
@Component(
        service = AsciidoctorService.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Provides asciidoctor business services",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
public class AsciidoctorService {

    private static final Logger log = LoggerFactory.getLogger(AsciidoctorService.class);

    private GlobalConfig globalConfig;
    private AsciidoctorPool asciidoctorPool;
    private ServiceResourceResolverProvider serviceResourceResolverProvider;

    @Activate
    public AsciidoctorService(
            @Reference GlobalConfig globalConfig,
            @Reference AsciidoctorPool asciidoctorPool,
            @Reference ServiceResourceResolverProvider serviceResourceResolverProvider) {
        this.globalConfig = globalConfig;
        this.asciidoctorPool = asciidoctorPool;
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
    }

    /**
     * Returns a document's html representation. If the document has not been built before, or if it is explicitly
     * requested, it is fully built. Otherwise a cached copy of the html is returned.
     *
     * @param document    The document for which to get the html representation
     * @param locale      The document locale
     * @param variantName The name of the document variant
     * @param draft       True if generating the draft version of the document. False if generating the released version.
     * @param context     any necessary context (attributes and their values) necessary to generate the html
     * @param forceRegen  when true, the html content is always re-generated; the cached content is ignored
     *                    This parameter is useful when passing context, as the cached content does not take
     *                    the context into account
     * @return The document's html representation based on its current asciidoc content
     */
    public String getDocumentHtml(@Nonnull Document document,
                                  @Nonnull Locale locale,
                                  @Nonnull String variantName,
                                  boolean draft,
                                  Map<String, Object> context,
                                  boolean forceRegen) {
        ResourceTraversal<? extends DocumentVariant> traversal = document.locale(locale)
                .traverse()
                .toChild(DocumentLocale::variants)
                .toChild(variants -> variants.variant(variantName));

        Optional<? extends DocumentVersion> moduleVersion;
        if (draft) {
            moduleVersion =
                    traversal.toChild(DocumentVariant::draft)
                            .getAsOptional();
        } else {
            moduleVersion =
                    traversal.toChild(DocumentVariant::released)
                            .getAsOptional();
        }

        String html;
        // If regeneration is forced, the content doesn't exist yet, or it needs generation because the original
        // asciidoc has changed,
        // then generate and save it
        // TODO To keep things simple, regeneration will not happen automatically when the source of the module
        //  has changed. This can be added later
        if (forceRegen
                || !moduleVersion.isPresent()
                || moduleVersion.get().cachedHtml().get() == null) {
            html = buildDocument(document, locale, variantName, draft, context, true);
        } else {
            html = moduleVersion.get()
                    .cachedHtml().get()
                    .jcrContent().get()
                    .jcrData().get();
        }

        return html;
    }

    /**
     * Builds a context Map that is initially populated from request parameters which are prefixed with "ctx_".
     *
     * @param request The http request provided by Sling
     * @return A Map object with all context parameters as keypairs, minus the "ctx_" prefix
     */
    public static Map<String, Object> buildContextFromRequest(SlingHttpServletRequest request) {
        // collect a list of parameter that traverseFrom with 'ctx_' as those will be used as asciidoctorj
        // parameters
        Map<String, Object> context = request.getRequestParameterList().stream().filter(
                p -> p.getName().toLowerCase().startsWith("ctx_")
        )
                .collect(toMap(
                        reqParam -> reqParam.getName().replaceFirst("ctx_", ""),
                        reqParam -> reqParam.getString())
                );
        return context;
    }

    /**
     * Builds a module. This means generating the html code for the module at one of its revisions.
     *
     * @param base          The base module. This should be the same module that the moduleVersion belongs to, but the code
     *                      won't check this. The module will only be used as a base for resolving included resources and images.
     * @param locale        The locale to build
     * @param variantName   The variant name to generate. If unknown, provide {@link ModuleVariant#DEFAULT_VARIANT_NAME}.
     * @param isDraft       True if aiming to generate the draft version of the module. False, to generate the released version.
     * @param context       Any asciidoc attributes necessary to inject into the generation process
     * @param regenMetadata If true, metadata will be extracted from the content and repopulated into the JCR module.
     * @return The generated html string.
     */
    private String buildDocument(@Nonnull Document base, @Nonnull Locale locale, @Nonnull String variantName, boolean isDraft,
                               Map<String, Object> context, final boolean regenMetadata) {

        Optional<HashableFileResource> sourceFile =
                traverseFrom(base)
                        .toChild(m -> m.locale(locale))
                        .toChild(DocumentLocale::source)
                        .toChild(sourceContent -> isDraft ? sourceContent.draft() : sourceContent.released())
                        .getAsOptional();

        if (!sourceFile.isPresent()) {
            throw new RuntimeException("Cannot find source content for module: " + base.getPath() + ", locale: " + locale
                    + ",variant: " + variantName + ", draft: " + isDraft);
        }

        // Use a service-level resource resolver to build the module or assemblies as it will require write access to the resources
        try (ResourceResolver serviceResourceResolver = serviceResourceResolverProvider.getServiceResourceResolver()) {

            Class cls = base.getResourceType().equals(PantheonConstants.RESOURCE_TYPE_ASSEMBLY) ? Assembly.class : Module.class;
            Document serviceDocument = (Document) SlingModels.getModel(serviceResourceResolver, base.getPath(), cls);

            DocumentVariant documentVariant = serviceDocument.locale(locale).getOrCreate()
                    .variants().getOrCreate()
                    .variant(variantName).getOrCreate();

            DocumentVersion documentVersion;
            if (isDraft) {
                documentVersion = documentVariant.draft().getOrCreate();
            } else {
                documentVersion = documentVariant.released().getOrCreate();
            }

            // process product and version.
            Optional<ProductVersion> productVersion =
                    documentVersion.metadata()
                            .traverse()
                            .toRef(DocumentMetadata::productVersion)
                            .getAsOptional();

            String productName = null;
            if (productVersion.isPresent()) {
                productName = productVersion.get().getProduct().name().get();
            }

            Calendar updatedDate = sourceFile.get().created().get();

            Optional<Calendar> publishedDate = documentVersion.metadata()
                    .traverse()
                    .toField(DocumentMetadata::datePublished);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMMM yyyy");

            String entitiesPath = base.getWorkspace().entities().get().getPath();
            Optional<String> attributesFilePath =
                    base.getWorkspace().moduleVariantDefinitions()
                            .traverse()
                            .toChild(vdf -> vdf.variant(variantName))
                            .toField(ModuleVariantDefinition::attributesFilePath);

            // build the attributes (default + those coming from http parameters)
            AttributesBuilder atts = AttributesBuilder.attributes()
                    // show the title on the generated html
                    .attribute("showtitle")
                    // show pantheonproduct on the generated html. Base the value from metadata.
                    .attribute("pantheonproduct", productName)
                    // show pantheonversion on the generated html. Base the value from metadata.
                    .attribute("pantheonversion", productVersion.isPresent() ? productVersion.get().name().get() : "")
                    // Shows custom rendering attribute to Haml
                    .attribute("pantheonenv", "preview")
                    // we want to avoid the footer on the generated html
                    .noFooter(true)
                    // link the css instead of embedding it
                    .linkCss(true)
                    // stylesheet reference
                    .styleSheetName("/static/rhdocs.css");

            if (attributesFilePath.isPresent()) {
                // provide attribute file as argument to ASCIIDOCTOR for building doc.
                if (PathUtils.isAbsolute(attributesFilePath.get())) {
                    // remove the starting slash
                    attributesFilePath = attributesFilePath.map(p -> p.substring(1));
                }
                atts.attribute("attsFile", PathUtils.concat(entitiesPath, attributesFilePath.get()));
            }

            if (updatedDate != null) {
                // show pantheonupdateddate on generated html. Base the value from metadata.
                atts.attribute("pantheonupdateddate", dateFormat.format(updatedDate.getTime()));
            }

            if (publishedDate.isPresent()) {
                // show pantheonpublisheddate on generated html. Base the value from metadata.
                atts.attribute("pantheonpublisheddate", dateFormat.format(publishedDate.get().getTime()));
            }

            // Add the context as attributes to the generation process
            context.entrySet().stream().forEach(entry -> {
                atts.attribute(entry.getKey(), entry.getValue());
            });

            // generate html
            OptionsBuilder ob = OptionsBuilder.options()
                    // we're generating html
                    .backend("html")
                    // no physical file is being generated
                    .toFile(false)
                    // allow for some extra flexibility
                    .safe(SafeMode.UNSAFE) // This probably needs to change
                    .inPlace(false)
                    // Generate the html header and footer
                    .headerFooter(true)
                    // use the provided attributes
                    .attributes(atts);
            globalConfig.getTemplateDirectory().ifPresent(ob::templateDir);

            long start = System.currentTimeMillis();
            Asciidoctor asciidoctor = asciidoctorPool.borrowObject();
            String html = "";
            try {
                final TableOfContents tableOfContents = new TableOfContents();
                // extensions needed to generate a module's html
                asciidoctor.javaExtensionRegistry().includeProcessor(
                        new SlingResourceIncludeProcessor(base, tableOfContents));

                asciidoctor.javaExtensionRegistry().preprocessor(
                        new XrefPreprocessor(documentVariant, tableOfContents));

                asciidoctor.javaExtensionRegistry().inlineMacro(MACRO_INCLUDE,
                        new PantheonIncludeProcessor(tableOfContents));

                asciidoctor.javaExtensionRegistry().postprocessor(
                        new HtmlModulePostprocessor(base));

                // add specific extensions for metadata regeneration
                if (regenMetadata) {
                    asciidoctor.javaExtensionRegistry().treeprocessor(
                            new MetadataExtractorTreeProcessor(documentVersion.metadata().getOrCreate()));
                }

                StringBuilder content = new StringBuilder();
                if (attributesFilePath.isPresent() && !isNullOrEmpty(attributesFilePath.get())) {
                    content.append("include::")
                            .append("{attsFile}")
                            .append("[]\r\n");
                }
                content.append(sourceFile.get()
                        .jcrContent().get()
                        .jcrData().get());
                html = asciidoctor.convert(content.toString(), ob.get());
                if (documentVersion instanceof AssemblyVersion) {
                    ((AssemblyVersion) documentVersion).consumeTableOfContents(tableOfContents);
                }
                cacheContent(documentVersion, html);

                // ack_status
                // TODO: re-evaluate where ack_status node should be created
                documentVersion.ackStatus().getOrCreate();
            } finally {
                asciidoctorPool.returnObject(asciidoctor);
            }
            log.info("Rendering finished in {} ms.", System.currentTimeMillis() - start);
            serviceResourceResolver.commit();

            return html;
        } catch (PersistenceException pex) {
            throw new RuntimeException(pex);
        }
    }

    /**
     * Stores (cache) the generated html content into the provided module for later retrieval. This method assumes
     * that the generated html is a result of the transformation of the Module's asciidoc content; but it will not
     * check this assertion.
     *
     * @param version The specific module version for which to cache the html
     * @param html    The html that was generated
     */
    private void cacheContent(final DocumentVersion version, final String html) {
        FileResource.JcrContent cachedHtmlFile = version.cachedHtml().getOrCreate()
                .jcrContent().getOrCreate();
        cachedHtmlFile.jcrData().set(html);
        cachedHtmlFile.mimeType().set("text/html");
    }
}
