package com.redhat.pantheon.asciidoctor;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.redhat.pantheon.asciidoctor.extension.ContentAbstractBlockProcessor;
import com.redhat.pantheon.asciidoctor.extension.MetadataExtractorTreeProcessor;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.module.Content;
import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.model.module.ModuleRevision;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
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
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
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
        String srcContent = content.asciidocContent.get();
        String existingHash = content.cachedHtml.get()
                .hash.get();

        return hash(srcContent).toString().equals(existingHash);
    }

    /**
     * Returns a module's html representation.
     * @param moduleRevision The module revision to generate content for
     * @param base The resource base (probably at the module level) to find included artifacts
     * @param context any necessary context (attributes and their values) necessary to generate the html
     * @param forceRegen when true, the html content is always re-generated; the cached content is ignored
     *                   This parameter is useful when passing context, as the cached content does not take
     *                   the context into account
     * @return The module's html representation based on its current asciidoc content
     */
    public String getModuleHtml(@Nonnull ModuleRevision moduleRevision,
                                @Nonnull Resource base,
                                Map<String, Object> context,
                                boolean forceRegen) {

        Content content = moduleRevision.content.get();
        Metadata metadata = moduleRevision.metadata.get();
        String html;
        // If regeneration is forced, the content doesn't exist yet, or it needs generation because the original
        // asciidoc has changed,
        // then generate and save it
        if( forceRegen || content.cachedHtml.get() == null || !generatedContentHashMatches(content) ) {
            html = generateHtml(content.asciidocContent.get(), base, context);
            cacheContent(content, html);
        } else {
            html = content.cachedHtml.get()
                    .data.get();
        }

        return html;
    }

    /**
     * Builds a context Map that is initially populated from request parameters which are prefixed with "ctx_".
     * @param request The http request provided by Sling
     * @return A Map object with all context parameters as keypairs, minus the "ctx_" prefix
     */
    public static Map<String, Object> buildContextFromRequest(SlingHttpServletRequest request) {
        // collect a list of parameter that start with 'ctx_' as those will be used as asciidoctorj
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
     * Extracts metadata from asciidoctor content and writes it to the metadata jcr node.
     * @param content The source node that contains the asciidoc content
     * @param metadata The destination node where the extracted metadata will be written
     */
    public void extractMetadata(FileResource.JcrContent content, Metadata metadata) {
        log.trace("=== Start extracting metadata ");
        long startTime = System.currentTimeMillis();
        Asciidoctor asciidoctor = asciidoctorPool.borrowObject();
        try {
            asciidoctor.javaExtensionRegistry().treeprocessor(
                    new MetadataExtractorTreeProcessor(metadata));
            asciidoctor.javaExtensionRegistry().block(
                    new ContentAbstractBlockProcessor(metadata));

            asciidoctor.load(content.jcrData.get(), newHashMap());
        }
        finally {
            asciidoctorPool.returnObject(asciidoctor);
        }
        long endTime = System.currentTimeMillis();
        log.trace("=== End extracting metadata. Time lapsed: " + (endTime-startTime)/1000 + " secs");
    }

    /**
     * Generates html content from an asciidoc string
     * @param asciidoc The asciidoc contents
     * @param base The base resource to use as 'current location' when generating the html. This is so that relative
     *             includes are able to be referenced in the JCR repository as they would when using asciidoctor
     *             from the command line.
     * @param context Any attributes necessary to inject into the generation process
     * @return The generated html for the provided asciidoc string and context
     */
    private String generateHtml(String asciidoc, Resource base, Map<String, Object> context) {

        // build the attributes (default + those coming from http parameters)
        AttributesBuilder atts = AttributesBuilder.attributes()
                // show the title on the generated html
                .attribute("showtitle")
                // link the css instead of embedding it
                .linkCss(true)
                // stylesheet reference
                .styleSheetName("/static/rhdocs.css");

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
        Asciidoctor asciidoctor = asciidoctorPool.borrowObject(base);
        String html = "";
        try {
            html = asciidoctor.convert(
                    asciidoc,
                    ob.get());
        } finally {
            asciidoctorPool.returnObject(asciidoctor);
        }
        log.info("Rendering finished in {} ms.", System.currentTimeMillis() - start);

        return html;
    }

    /**
     * Stores (cache) the generated html content into the provided module for later retrieval. This method assumes
     * that the generated html is a result of the transformation of the Module's asciidoc content; but it will not
     * check this assertion.
     * @param content The module's content instance on which to cache the content.
     * @param html The html that was generated
     */
    private void cacheContent(final Content content, final String html) {
        try (ResourceResolver serviceResourceResolver = serviceResourceResolverProvider.getServiceResourceResolver()) {
            String asciidoc = content.asciidocContent.get();
            // reload from the service-level resolver
            Content writeableContent =
                    serviceResourceResolver.getResource(content.getPath()).adaptTo(Content.class);

            writeableContent.cachedHtml.getOrCreate()
                    .hash.set(
                        hash(asciidoc).toString()
                    );
            writeableContent.cachedHtml.getOrCreate()
                    .data.set(html);
            serviceResourceResolver.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * calculates a hash for a string
     * TODO This should probably be moved elsewhere
     */
    private HashCode hash(String str) {
        return Hashing.adler32().hashString(str == null ? "" : str, Charsets.UTF_8);
    }
}
