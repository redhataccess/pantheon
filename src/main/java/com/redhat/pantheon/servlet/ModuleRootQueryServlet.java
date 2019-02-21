package com.redhat.pantheon.servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.annotation.Nonnull;
import javax.jcr.query.Query;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;

/**
 * This query servlet applies to any resource with type 'pantheon/modules/root'. It enables an action under a resource
 * which searches for modules (i.e. resources of jcr type 'pant:module2' which match several parameters.
 * Currently the only parameter being used is the node name.
 *
 * To enable this servlet, simple add the sling:resourceType property to any node under which you wish to search.
 *
 * The node can now be searched at the following endpoint (assuming the node location is /content/mynode):
 *
 * GET /content/mynode.query.json?name=astring
 *
 * where the name parameter is a substring to search for in the node's name.
 *
 * In case where multiple results are found, only the first module will be returned.
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=pantheon/modules/root",
                "sling.servlet.selectors=query",
                "sling.servlet.extensions=json",
                Constants.SERVICE_DESCRIPTION + "=Servlet that searches for modules under a module root",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
public class ModuleRootQueryServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {

        // Get the query parameter(s)
        String moduleName = request.getParameter("name");

        Iterator<Resource> resources = request.getResourceResolver().findResources("SELECT * from [pant:module2] AS modules WHERE ISDESCENDANTNODE(\"" +
                request.getResource().getPath() + "\") " +
                "and name(modules) like '%" + moduleName + "%'", Query.JCR_SQL2);

        if (!resources.hasNext()) {
            response.sendError(404, "No modules found");
        } else {
            // Redirect to the first found module
            RequestDispatcherOptions options = new RequestDispatcherOptions();
            options.setReplaceSelectors("");

            request.getRequestDispatcher(resources.next(), options)
                    .forward(request, response);
        }
    }
}
