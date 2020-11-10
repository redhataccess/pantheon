package com.redhat.pantheon.servlet.assembly;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.model.assembly.Assembly;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentMetadata;
import com.redhat.pantheon.servlet.module.ModuleVersionUpload;
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

@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet POST operation which accepts module uploads and versions them appropriately",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:newAssemblyVersion"
        })
public class AssemblyVersionUpload extends VersionUploadOperation {

    private AsciidoctorService asciidoctorService;

    @Activate
    public AssemblyVersionUpload(
            @Reference AsciidoctorService asciidoctorService) {
        this.asciidoctorService = asciidoctorService;
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {

        try {
            runCommon(request, response, asciidoctorService, Assembly.class);
        } catch (Exception e) {
            throw new RepositoryException("Error uploading an assembly version", e);
        }
    }

    @Override
    protected void performTypeSpecficExtras(Document document, DocumentMetadata draftMetadata) {}
}
