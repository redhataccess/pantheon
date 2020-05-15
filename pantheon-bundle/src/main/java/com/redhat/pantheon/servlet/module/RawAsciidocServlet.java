package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleLocale;
import com.redhat.pantheon.model.module.SourceContent;
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
import static com.redhat.pantheon.model.api.util.ResourceTraversal.start;
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
        Module module = resource.adaptTo(Module.class);

        response.setContentType("html");
        Writer w = response.getWriter();

        Optional<String> content;
        if (draft) {
            content = start(module)
                    .traverse(m -> m.moduleLocale(locale))
                    .traverse(ModuleLocale::source)
                    .traverse(SourceContent::draft)
                    .traverse(FileResource::jcrContent)
                    .field(FileResource.JcrContent::jcrData);
        } else {
            content = start(module)
                    .traverse(m -> m.moduleLocale(locale))
                    .traverse(ModuleLocale::source)
                    .traverse(SourceContent::released)
                    .traverse(FileResource::jcrContent)
                    .field(FileResource.JcrContent::jcrData);
        }

        if(content.isPresent()) {
            response.setContentType(ContentType.TEXT_PLAIN.toString());
            w.write(content.get());
        } else {
            response.sendError(SC_NOT_FOUND, "Requested content not found for locale " + locale.toString()
                    + " and in state " + (draft ? "'draft'" : "released"));
        }
    }
}
