package com.redhat.pantheon.servlet;

import com.redhat.pantheon.model.Module;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
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

/**
 * Renders the asciidoc content exactly as stored.
 * (Use a browser plugin to watch it turn into HTML)
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=pantheon/modules",
                "sling.servlet.extensions=adoc",
                Constants.SERVICE_DESCRIPTION+"=Renders asciidoc content in its raw original form",
                Constants.SERVICE_VENDOR+"=Red Hat Content Tooling team"
        }
)
public class AsciidocContentRenderingServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(AsciidocContentRenderingServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        Resource resource = request.getResource();
        Module module = resource.adaptTo(Module.class);

        response.setContentType("html");
        Writer w = response.getWriter();
        w.write(module.asciidocContent.get());
    }
}
