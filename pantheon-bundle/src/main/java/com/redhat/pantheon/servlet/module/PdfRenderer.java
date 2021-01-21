package com.redhat.pantheon.servlet.module;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.module.Module;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which transforms asciidoc content into pdf",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletResourceTypes(
        resourceTypes = {"pantheon/module"},
        methods = "GET",
        extensions = "pdf")
public class PdfRenderer extends SlingSafeMethodsServlet {

    @Reference
    AsciidoctorService asciidoctorService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        Module module = request.getResource().adaptTo(Module.class);

        InputStream pdfFile =
                asciidoctorService.buildDocumentPdf(module, GlobalConfig.DEFAULT_MODULE_LOCALE,
                    DocumentVariant.DEFAULT_VARIANT_NAME, true, Maps.newHashMap(), true);

        response.setStatus(200);
        response.setContentType("application/pdf");
        ByteStreams.copy(pdfFile, response.getOutputStream());

        // TODO at this point the temp file still exists. Figure out what to do with it.
    }
}
