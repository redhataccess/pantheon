package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentLocale;
import com.redhat.pantheon.model.document.SourceContent;
import com.redhat.pantheon.model.module.HashableFileResource;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.http.entity.ContentType;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Optional;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsBoolean;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsLocale;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Renders the asciidoc content exactly as stored.
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_VENDOR+"=Red Hat Content Tooling team"
        }
)
@SlingServlet(
        methods = "GET",
        extensions = "raw",
        resourceTypes = "pantheon/module",
        description = "Renders asciidoc content in its raw original form"
)
public class RawAsciidocServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(RawAsciidocServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        Locale locale = paramValueAsLocale(request, "locale", DEFAULT_MODULE_LOCALE);
        boolean draft = paramValueAsBoolean(request, "draft");

        Resource resource = request.getResource();
        Document document = resource.adaptTo(Document.class);

        response.setContentType("html");
        Writer w = response.getWriter();

        Optional<String> content = traverseFrom(document)
                .toChild(m -> m.locale(locale))
                .toChild(DocumentLocale::source)
                .toChild(draft ? SourceContent::draft : SourceContent::released)
                .toChild(FileResource::jcrContent)
                .toField(FileResource.JcrContent::jcrData);

        if(content.isPresent()) {
            response.setContentType(ContentType.TEXT_PLAIN.toString());
            w.write(content.get());
        } else {
            response.sendError(SC_NOT_FOUND, "Requested content not found for locale " + locale.toString()
                    + " and in state " + (draft ? "'draft'" : "released"));
        }
    }
}
