package com.redhat.pantheon.servlet;

import com.redhat.pantheon.jcr.JcrQueryHelper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
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

    private static final String PANTHEON_PATH = "/content/pantheon";

    private final Logger log = LoggerFactory.getLogger(PathNotFoundServlet.class);

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        log.info("Running PathNotFoundServlet doGet on path {}", path);
        String[] parts = path.split("/");
        String resourceName = parts[parts.length - 1];
        log.info("Resource name: " + resourceName);

        if (resourceName.contains(".")) {
            //Probably a request for a file in /content/pantheon
            StringBuilder sb = new StringBuilder();
            sb.append("select * from [nt:file] as a where name() = '")
                    .append(resourceName)
                    .append("' and isdescendantnode(a, '" + PANTHEON_PATH + "')");

            log.debug("query: {}", sb);

            try {
                Object[] results = new JcrQueryHelper(request.getResourceResolver()).query(sb.toString(), 1, 0).toArray();
                if (results.length > 0) {
                    String newPath = ((Resource) results[0]).getPath();
                    log.info("Fulfilling request with {}", newPath);
                    response.sendRedirect(newPath);
                } else {
                    log.warn("File-based resource not found, delivering 404: {}", path);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (RepositoryException e) {
                log.error("Repository exception", e);
            }
        } else {
            //Probably a request for a react route, so just deliver index.html
            log.debug("Request {} does not appear file-based; delivering index.html", path);
            Resource index = request.getResourceResolver().getResource(PANTHEON_PATH + "/index.html/jcr:content");
            String indexHtml = index.getValueMap().get("jcr:data", String.class);

            response.setContentType("text/html");
            response.getWriter().write(indexHtml);
        }
    }
}
