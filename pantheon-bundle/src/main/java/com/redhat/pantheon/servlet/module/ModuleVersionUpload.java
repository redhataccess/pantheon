package com.redhat.pantheon.servlet.module;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.HashableFileResource;
import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleLocale;
import com.redhat.pantheon.model.module.ModuleType;
import com.redhat.pantheon.servlet.ServletUtils;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.servlets.post.AbstractPostOperation;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.PostOperation;
import org.apache.sling.servlets.post.PostResponse;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Post operation to add a new Module version to the system.
 * The expected parameters in the post request are:
 * 1. locale - Optional; indicates the locale that the module content is in
 * 2. :operation - This value must be 'pant:newModuleVersion'
 * 3. asciidoc - The file upload (multipart) containing the asciidoc content file for the new module version.
 *
 * The url to POST a request to the server is the path of the new or existing module to host the content.
 * If there is no content for said url, the module is created and a single version along with it.
 *
 * @author Carlos Munoz
 */
@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet POST operation which accepts module uploads and versions them appropriately",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:newModuleVersion"
        })
public class ModuleVersionUpload extends AbstractPostOperation {

    private static final Logger log = LoggerFactory.getLogger(ModuleVersionUpload.class);

    private AsciidoctorService asciidoctorService;
    private ServiceResourceResolverProvider serviceResourceResolverProvider;

    @Activate
    public ModuleVersionUpload(
            @Reference AsciidoctorService asciidoctorService,
            @Reference ServiceResourceResolverProvider serviceResourceResolverProvider) {
        this.asciidoctorService = asciidoctorService;
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {

        try {
            String locale = ServletUtils.paramValue(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE.toString());
            String asciidocContent = ServletUtils.paramValue(request, "asciidoc");

            String encoding = request.getCharacterEncoding();
            if (encoding != null) {
                asciidocContent = new String(asciidocContent.getBytes(encoding), StandardCharsets.UTF_8);
            }

            String path = request.getResource().getPath();

            log.debug("Pushing new module version at: " + path + " with locale: " + locale);
            log.trace("and content: " + asciidocContent);
            int responseCode = HttpServletResponse.SC_OK;

            // Try to find the module
            ResourceResolver resolver = request.getResourceResolver();
            Resource moduleResource = resolver.getResource(path);
            Module module;
            if(moduleResource == null) {
                module =
                        SlingModels.createModel(
                                resolver,
                                path,
                                Module.class);
                responseCode = HttpServletResponse.SC_CREATED;
            } else {
                module = moduleResource.adaptTo(Module.class);
            }

            Locale localeObj = LocaleUtils.toLocale(locale);
            ModuleLocale moduleLocale = module.moduleLocale(localeObj).getOrCreate();
            HashableFileResource draftSrc = moduleLocale
                    .source().getOrCreate()
                    .draft().getOrCreate();

            // Check if the content is the same as what is hashed already
            HashCode incomingSrcHash = hash(asciidocContent);
            String storedSrcHash = draftSrc.hash().get();
            // If the source content is the same, don't update it
            if(incomingSrcHash.toString().equals( storedSrcHash )) {
                responseCode = HttpServletResponse.SC_NOT_MODIFIED;
            } else {
                draftSrc.jcrContent().getOrCreate()
                        .jcrData().set(asciidocContent);
                draftSrc.jcrContent().getOrCreate()
                        .mimeType().set("text/x-asciidoc");

                resolver.commit();

                Map<String, Object> context = asciidoctorService.buildContextFromRequest(request);
                asciidoctorService.getModuleHtml(module, localeObj, module.getWorkspace().getCanonicalVariantName(),
                        true, context, true);

                Metadata metadata = moduleLocale
                        .variants().getOrCreate()
                        .variant(
                                moduleLocale.getWorkspace().getCanonicalVariantName())
                        .getOrCreate()
                        .draft().getOrCreate()
                        .metadata().getOrCreate();
                metadata.dateModified().set(Calendar.getInstance());
                // Generate a module type based on the file name ONLY after asciidoc generation, so that the
                // attribute-based logic takes precedence
                if(metadata.moduleType().get() == null) {
                    metadata.moduleType().set(determineModuleType(module));
                }
            }

            resolver.commit();
            response.setStatus(responseCode, "");
        } catch (Exception e) {
            throw new RepositoryException("Error uploading a module version", e);
        }
    }

    /**
     * Determines the module type from the uploaded module version
     * @param module The uploaded module
     * @return A module type for the module version, or null if one cannot be determined.
     */
    private static ModuleType determineModuleType(Module module) {
        String fileName = module.getName();

        if( fileName.startsWith("proc_") ) {
            return ModuleType.PROCEDURE;
        }
        else if( fileName.startsWith("con_") ) {
            return ModuleType.CONCEPT;
        }
        else if( fileName.startsWith("ref_") ) {
            return ModuleType.REFERENCE;
        }
        else {
            return null;
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
