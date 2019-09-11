package com.redhat.pantheon.servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.redhat.pantheon.servlet.ServletUtils.*;

/**
 * Simple servlet which aims to provide an internal way to quickly access resources by their UUID. This servlet is
 * read-only, and the returned content is in json format. This servlet also offers the capability of deep diving into
 * the resource's children by providing a 'depth' parameter.
 *
 * @author Carlos Munoz
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which allows querying of any object via their UUID",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/pantheon/internal/node.json")
public class GetObjectByUUID extends SlingSafeMethodsServlet {

    final String UUID_PARAM = "uuid";
    final String DEPTH_PARAM = "depth";

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response)
            throws ServletException, IOException {
        String uuid = paramValue(request, UUID_PARAM);
        Long depth = paramValueAsLong(request, DEPTH_PARAM, 0L);

        if(isNullOrEmpty(uuid)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter '" + UUID_PARAM + "' must be provided");
            return;
        } else if (depth < 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter '" + DEPTH_PARAM + "' must be >= 0");
            return;
        }

        try {
            Node foundNode = request.getResourceResolver()
                    .adaptTo(Session.class)
                    .getNodeByIdentifier(uuid);

            // turn the node back into a resource
            Resource foundResource = request.getResourceResolver()
                    .getResource(foundNode.getPath());
            RequestDispatcherOptions options = new RequestDispatcherOptions();
            options.setAddSelectors("." + depth);
            RequestDispatcher requestDispatcher =
                    request.getRequestDispatcher(foundResource.getPath() + ".json", options);
            requestDispatcher.include(request, response);
        } catch (ItemNotFoundException infex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (RepositoryException e) {
            throw new ServletException(e);
        }
    }
}
