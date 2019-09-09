package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.FileResource.JcrContent;
import com.redhat.pantheon.model.api.SlingResourceUtil;
import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleRevision;
import com.redhat.pantheon.model.module.ModuleType;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Post operation to add a new Module revision to the system.
 * Only thre parameters are expected in the post request:
 * 1. locale - Optional; indicates the locale that the module content is in
 * 2. :operation - This value must be 'pant:newModuleRevision'
 * 3. asciidoc - The file upload (multipart) containing the asciidoc content file for the new module revision.
 *
 * The url to POST a request to the server is the path of the new or existing module to host the content.
 * If there is no content for said url, the module is created and a single revision along with it.
 *
 * @author Carlos Munoz
 */
@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet POST operation which accepts module uploads and versions them appropriately",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:newModuleRevision"
        })
public class ModuleRevisionUpload extends AbstractPostOperation {

    private static final Logger log = LoggerFactory.getLogger(ModuleRevisionUpload.class);

    private AsciidoctorService asciidoctorService;

    @Activate
    public ModuleRevisionUpload(@Reference AsciidoctorService asciidoctorService) {
        this.asciidoctorService = asciidoctorService;
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {

        try {
            String locale = ServletUtils.paramValue(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE.toString());
            String asciidocContent = ServletUtils.paramValue(request, "asciidoc");
            String path = request.getResource().getPath();
            String moduleName = ResourceUtil.getName(path);
            String description = ServletUtils.paramValue(request, "jcr:description", "");

            log.debug("Pushing new module revision at: " + path + " with locale: " + locale);
            log.trace("and content: " + asciidocContent);

            // Try to find the module
            Resource moduleResource = request.getResourceResolver().getResource(path);
            Module module;

            if(moduleResource == null) {
                module =
                        SlingResourceUtil.createNewSlingResource(
                                request.getResourceResolver(),
                                path,
                                Module.class);
            } else {
                module = moduleResource.adaptTo(Module.class);
            }

            Locale localeObj = LocaleUtils.toLocale(locale);
            Optional<ModuleRevision> draftRevision = module.getDraftRevision(localeObj);
            // if there is no draft content, create it
            if( !draftRevision.isPresent() ) {
                draftRevision = Optional.of(
                        module.getOrCreateModuleLocale(localeObj)
                        .createNextRevision());
                module.getOrCreateModuleLocale(localeObj)
                        .draft.set( draftRevision.get().uuid.get() );
            }

            // modify only the draft content/metadata
            JcrContent jcrContent = draftRevision.get()
                    .content.getOrCreate()
                    .asciidoc.getOrCreate()
                    .jcrContent.getOrCreate();
            boolean generateHtml = false;
            String jcrData = jcrContent.jcrData.get();

            if ((jcrData != null && !jcrData.equals(asciidocContent)) || !draftRevision.map(i -> i.content.get()).map(i -> i.cachedHtml.get()).isPresent()) {
                generateHtml = true;
            }
            jcrContent.jcrData.set(asciidocContent);
            jcrContent.mimeType.set("text/x-asciidoc");

            Metadata metadata = draftRevision.get()
                    .metadata.getOrCreate();
            metadata.title.set(moduleName);
            metadata.description.set(description);
            metadata.dateModified.set(Calendar.getInstance());
            metadata.moduleType.set( determineModuleType(module) );

            asciidoctorService.extractMetadata(jcrContent, metadata);

            request.getResourceResolver().commit();

            if (generateHtml) {
                Map<String, Object> context = asciidoctorService.buildContextFromRequest(request);
                // drop the html on the floor, this is just to cache the results
                asciidoctorService.getModuleHtml(draftRevision.get(), module, context, true);
            }
        } catch (Exception e) {
            throw new RepositoryException("Error uploading a module revision", e);
        }
    }

    /**
     * Determines the module type from the uploaded module revision
     * @param module The uploaded module
     * @return A module type for the module revision, or null if one cannot be determined.
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
}
