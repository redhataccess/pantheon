package com.redhat.pantheon.asciidoctor;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.redhat.pantheon.asciidoctor.extension.HtmlModulePostprocessor;
import com.redhat.pantheon.asciidoctor.extension.MetadataExtractorTreeProcessor;
import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.module.*;
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
import java.util.function.Supplier;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;
import static java.util.Optional.empty;
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
     * Indicates if the generated html content for a Module matches the stored hash.
     * This method serves as an indicator of whether the asciidoc content has been updated
     * and hence the resulting html needs to be re-generated.
     * @param content the module content
     * @return true if the generated html content was generated from the Module's asciidoc
     * content. False otherwise.
     */
    private boolean generatedContentHashMatches(Content content) {
        String srcContent = content.asciidocContent().get();
        String existingHash = content.cachedHtml().get()
                .hash().get();

        return hash(srcContent).toString().equals(existingHash);
    }

    /**
     * Returns a module's html representation. If the module has not been built before, or if it is explicitly requested,
     * it is fully built. Otherwise a cached copy of the html is returned.
     * @param module The module for which to get the html representation
     * @param locale The module locale
     * @param variantName The name of the module variant
     * @param draft True if generating the draft version of the module. False if generating the released version.
     * @param context any necessary context (attributes and their values) necessary to generate the html
     * @param forceRegen when true, the html content is always re-generated; the cached content is ignored
     *                   This parameter is useful when passing context, as the cached content does not take
     *                   the context into account
     * @return The module's html representation based on its current asciidoc content
     */
    public String getModuleHtml(@Nonnull Module module,
                                @Nonnull Locale locale,
                                @Nonnull String variantName,
                                boolean draft,
                                Map<String, Object> context,
                                boolean forceRegen) {

        Optional<HashableFileResource> sourceFile =
                traverseFrom(module)
                        .toChild(m -> m.moduleLocale(locale))
                        .toChild(ModuleLocale::source)
                        .toChild(sourceContent -> draft ? sourceContent.draft() : sourceContent.released())
                        .getAsOptional();

        if (!sourceFile.isPresent()) {
            throw new RuntimeException("source file for locale " + locale + " module " + module.getPath() + " variant "
                    + variantName + " draft " + draft);
        }


        ModuleVariant moduleVariant = module.moduleLocale(locale).getOrCreate()
                .variants().getOrCreate()
                .variant(variantName).getOrCreate();

        ModuleVersion moduleVersion;
        if (draft) {
            moduleVersion = moduleVariant.draft().getOrCreate();
        } else {
            moduleVersion = moduleVariant.released().getOrCreate();
        }

        String html;
        // If regeneration is forced, the content doesn't exist yet, or it needs generation because the original
        // asciidoc has changed,
        // then generate and save it
        // TODO To keep things simple, regeneration will not happen automatically when the source of the module
        //  has changed. This can be added later
        if( forceRegen || moduleVersion.cachedHtml().get() == null ) {
            html = buildModule(module, locale, variantName, draft, sourceFile.get(), context, true);
        } else {
            html = moduleVersion
                    .cachedHtml().get()
                    .jcrContent().get()
                    .jcrData().get();
        }

        return html;
    }

    /**
     * Builds a context Map that is initially populated from request parameters which are prefixed with "ctx_".
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
     * @param base The base module. This should be the same module that the moduleVersion belongs to, but the code
     *             won't check this. The module will only be used as a base for resolving included resources and images.
     * @param moduleVersion The actual module version that is being generated. It should have its asciidoc content present.
     * @param context Any asciidoc attributes necessary to inject into the generation process
     * @param regenMetadata If true, metadata will be extracted from the content and repopulated into the JCR module.
     * @return The generated html string.
     */
    private String buildModule(Module base, FileResource sourceContent,
                               Map<String, Object> context, final boolean regenMetadata) {

        // Use a service-level resource resolver to build the module as it will require write access to the resources
        try (ResourceResolver serviceResourceResolver = serviceResourceResolverProvider.getServiceResourceResolver()) {
            moduleVersion = serviceResourceResolver.getResource(moduleVersion.getPath()).adaptTo(ModuleVersion.class);

            // process product and version.
            Optional<ProductVersion> productVersion = empty();
            if (moduleVersion.metadata().get().getValueMap().containsKey("productVersion")) {
                productVersion = moduleVersion.metadata()
                        .traverse()
                        .toRef(Metadata::productVersion)
                        .getAsOptional();
            }

            String productName = null;
            if (productVersion.isPresent()) {
                productName = productVersion.get().getProduct().name().get();
            }

            Calendar updatedDate = sourceContent.created().get();

            Optional<Calendar> publishedDate = moduleVersion.metadata()
                    .map(Metadata::datePublished)
                    .map(Supplier::get);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMMM yyyy");

            String entitiesPath = moduleVersion.getWorkspace().entities().get().getPath();
            String variantName = moduleVersion.getParent().getName();
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
                    // we want to avoid the footer on the generated html
                    .noFooter(true)
                    // link the css instead of embedding it
                    .linkCss(true)
                    // stylesheet reference
                    .styleSheetName("/static/rhdocs.css");

            if(attributesFilePath.isPresent()) {
                // provide attribute file as argument to ASCIIDOCTOR for building doc.
                if( PathUtils.isAbsolute(attributesFilePath.get()) ) {
                    // remove the starting slash
                    attributesFilePath = attributesFilePath.map(p -> p.substring(1));
                }
                atts.attribute("attsFile", PathUtils.concat(entitiesPath, attributesFilePath.get()));
            }

            if(updatedDate != null) {
                // show pantheonupdateddate on generated html. Base the value from metadata.
                atts.attribute("pantheonupdateddate",  dateFormat.format(updatedDate.getTime()));
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
                // extensions needed to generate a module's html
                asciidoctor.javaExtensionRegistry().includeProcessor(
                        new SlingResourceIncludeProcessor(base));
                asciidoctor.javaExtensionRegistry().postprocessor(
                        new HtmlModulePostprocessor(base));

                // add specific extensions for metadata regeneration
                if(regenMetadata) {
                    asciidoctor.javaExtensionRegistry().treeprocessor(
                            new MetadataExtractorTreeProcessor(moduleVersion.metadata().getOrCreate()));
                }

                StringBuilder content = new StringBuilder();
                if (attributesFilePath.isPresent() && !isNullOrEmpty(attributesFilePath.get())) {
                    content.append("include::")
                            .append("{attsFile}")
                            .append("[]\n");
                }
                content.append(sourceContent.jcrContent().get().jcrData().get());
                html = asciidoctor.convert(content.toString(), ob.get());
                cacheContent(moduleVersion, html);
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
     * @param version The specific module version for which to cache the html
     * @param html The html that was generated
     */
    private void cacheContent(final ModuleVersion version, final String html) {
        version.cachedHtml().getOrCreate()
                .jcrContent().getOrCreate()
                .jcrData().set(html);
    }

    /*
     * calculates a hash for a string
     * TODO This should probably be moved elsewhere
     */
    private HashCode hash(String str) {
        return Hashing.adler32().hashString(str == null ? "" : str, Charsets.UTF_8);
    }
}
