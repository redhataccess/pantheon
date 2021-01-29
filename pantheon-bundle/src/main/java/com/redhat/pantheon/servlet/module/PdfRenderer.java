package com.redhat.pantheon.servlet.module;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.document.Document;
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
import java.io.IOException;
import java.io.InputStream;

/**
 * Rendering servlet for PDFs
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which transforms asciidoc content into pdf",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletResourceTypes(
        resourceTypes = {"pantheon/module", "pantheon/assembly"},
        methods = "GET",
        extensions = "pdf")
public class PdfRenderer extends SlingSafeMethodsServlet {

    @Reference
    AsciidoctorService asciidoctorService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        Document document = request.getResource().adaptTo(Document.class);

        InputStream pdfFile =
                asciidoctorService.buildDocumentPdf(document, GlobalConfig.DEFAULT_MODULE_LOCALE,
                    DocumentVariant.DEFAULT_VARIANT_NAME, true, Maps.newHashMap(), true);

        response.setStatus(200);
        response.setContentType("application/pdf");
        ByteStreams.copy(pdfFile, response.getOutputStream());
    }
}
