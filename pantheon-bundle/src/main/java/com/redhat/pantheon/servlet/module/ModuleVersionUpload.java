package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentMetadata;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleMetadata;
import com.redhat.pantheon.model.module.ModuleType;
import com.redhat.pantheon.servlet.util.VersionUploadOperation;
import org.apache.sling.api.SlingHttpServletRequest;
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
import java.util.List;

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
public class ModuleVersionUpload extends VersionUploadOperation {

    private AsciidoctorService asciidoctorService;

    @Activate
    public ModuleVersionUpload(
            @Reference AsciidoctorService asciidoctorService) {
        this.asciidoctorService = asciidoctorService;
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {

        try {
            runCommon(request, response, asciidoctorService, Module.class);
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

    @Override
    protected void performTypeSpecficExtras(Document document, DocumentMetadata draftMetadata) {
        ModuleMetadata meta = (ModuleMetadata) draftMetadata;
        if(meta.moduleType().get() == null) {
            meta.moduleType().set(determineModuleType((Module) document));
        }
    }
}
