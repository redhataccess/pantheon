package com.redhat.pantheon.servlet;

import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

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
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        Resource resource = request.getResource();

        String content = resource.getValueMap().get("pantheon:asciidocContent", String.class);

        response.setContentType("text/adoc");
        Writer w = response.getWriter();
        w.write(content);
    }
}
