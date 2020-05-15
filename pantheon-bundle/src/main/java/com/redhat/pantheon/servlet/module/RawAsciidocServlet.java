package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.module.Module;
import org.apache.commons.lang3.LocaleUtils;
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
import java.util.Optional;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.model.module.ModuleVariant.DEFAULT_VARIANT_NAME;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsBoolean;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Renders the asciidoc content exactly as stored.
 * (Use a browser plugin to watch it turn into HTML)
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=pantheon/module",
                "sling.servlet.extensions=raw",
                Constants.SERVICE_DESCRIPTION+"=Renders asciidoc content in its raw original form",
                Constants.SERVICE_VENDOR+"=Red Hat Content Tooling team"
        }
)
public class AsciidocContentRenderingServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(AsciidocContentRenderingServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        String locale = paramValue(request, "locale", DEFAULT_MODULE_LOCALE.toString());
        boolean draft = paramValueAsBoolean(request, "draft");
        String variant = paramValue(request, "variant", DEFAULT_VARIANT_NAME);

        Resource resource = request.getResource();
        Module module = resource.adaptTo(Module.class);

        response.setContentType("html");
        Writer w = response.getWriter();

        Optional<FileResource> content;
        if (draft) {
            content = module.getDraftContent(LocaleUtils.toLocale(locale), variant);
        } else {
            content = module.getReleasedContent(LocaleUtils.toLocale(locale), variant);
        }

        if(content.isPresent()) {
            response.setContentType(ContentType.TEXT_PLAIN.toString());
            w.write(content.get().jcrContent().get().jcrData().get());
        } else {
            response.sendError(SC_NOT_FOUND, "Requested content not found for locale " + locale.toString()
                    + " and in state " + (draft ? "'draft'" : "released"));
        }
    }
}
