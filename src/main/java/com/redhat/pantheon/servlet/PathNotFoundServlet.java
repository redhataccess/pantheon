package com.redhat.pantheon.servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.engine.servlets.ErrorHandler;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by ben on 5/30/19.
 *
 * https://sling.apache.org/documentation/the-sling-engine/errorhandling.html
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Handles resource-not-found errors by delivering /index.html.",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/apps/sling/servlet/errorhandler/default")
public class PathNotFoundServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(PathNotFoundServlet.class);

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        log.info("Running PathNotFoundServlet doGet on path {}", path);

        boolean reactResource = false;
        //FIXME - hardcoding app.css, app.bundle.js, and pantheon is bad. Open to better ideas.
        for (String suffix : new String[] { "/app.css", "/app.bundle.js" }) {
            if (path.endsWith(suffix)) {
                reactResource = true;
                response.sendRedirect("/pantheon" + suffix);
            }
        }

        if (!reactResource) {
            //FIXME - hardcoding this path is also probably bad
            Resource index = request.getResourceResolver().getResource("/content/pantheon/index.html/jcr:content");
            String indexHtml = index.getValueMap().get("jcr:data", String.class);

            response.setContentType("text/html");
            response.getWriter().write(indexHtml);
        }
    }
}
