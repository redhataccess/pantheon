package com.redhat.pantheon.asciidoctor;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.redhat.pantheon.conf.LocalFileManagementService;
import com.redhat.pantheon.model.Module;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
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

import java.io.IOException;
import java.util.Map;

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

    private LocalFileManagementService localFileManagementService;
    private AsciidoctorPool asciidoctorPool;
    private ServiceResourceResolverProvider serviceResourceResolverProvider;

    @Activate
    public AsciidoctorService(
            @Reference LocalFileManagementService localFileManagementService,
            @Reference AsciidoctorPool asciidoctorPool,
            @Reference ServiceResourceResolverProvider serviceResourceResolverProvider) {
        this.localFileManagementService = localFileManagementService;
        this.asciidoctorPool = asciidoctorPool;
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
    }

    /**
     * Indicates if the generated html content for a Module matches the stored hash.
     * This method serves as an indicator of whether the asciidoc content has been updated
     * and hence the resulting html needs to be re-generated.
     * @param module the module in question
     * @return true if the generated html content was generated from the Module's asciidoc
     * content. False otherwise.
     */
    private boolean generatedContentHashMatches(Module module) {
        String srcContent = module.asciidocContent.get();
        String existingHash = module.cachedContent.get()
                .hash.get();

        return hash(srcContent).toString().equals(existingHash);
    }

    /**
     * Returns a module's html representation.
     * @param module the module from which to return the html content
     * @param context any necessary context (attributes and their values) necessary to generate the html
     * @param forceRegen when true, the html content is always re-generated; the cached content is ignored
     *                   This parameter is useful when passing context, as the cached content does not take
     *                   the context into account
     * @return The module's html representation based on its current asciidoc content
     * @throws IOException If there is a problem generating the html content
     */
    public String getModuleHtml(Module module, Map<String, Object> context, boolean forceRegen) throws IOException {
        String html = module.cachedHtmlContent.get();

        // If regeneration is forced, the content doesn't exist yet, or it needs generation because the original
        // asciidoc has changed,
        // then generate and save it
        if( forceRegen || html == null || !generatedContentHashMatches(module) ) {
            html = generateHtml(module.asciidocContent.get(), module.getResource(), context);
            cacheContent(module, html);
        }

        return html;
    }

    /**
     * Generates html content from an asciidoc string
     * @param asciidoc The asciidoc contents
     * @param base The base resource to use as 'current location' when generating the html. This is so that relative
     *             includes are able to be referenced in the JCR repository as they would when using asciidoctor
     *             from the command line.
     * @param context Any attributes necessary to inject into the generation process
     * @return The generated html for the provided asciidoc string and context
     * @throws IOException If there is a problem generating the html
     */
    private String generateHtml(String asciidoc, Resource base, Map<String, Object> context) throws IOException {

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
        localFileManagementService.getTemplateDirectory().ifPresent(ob::templateDir);

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
     * @param module The module on which to cache the content.
     * @param html The html that was generated
     */
    private void cacheContent(final Module module, final String html) {
        try {
            String asciidoc = module.asciidocContent.get();
            ResourceResolver serviceResourceResolver = serviceResourceResolverProvider.getServiceResourceResolver();
            // reload from the service-level resolver
            Module writeableModule = serviceResourceResolver.getResource(module.getResource().getPath())
                    .adaptTo(Module.class);

            writeableModule.cachedContent.get()
                    .hash.set(
                        hash(asciidoc).toString()
                    );
            writeableModule.cachedContent.get()
                    .data.set(html);
            serviceResourceResolver.commit();
            serviceResourceResolver.close();
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
