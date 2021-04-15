package com.redhat.pantheon.servlet.alias;

import com.redhat.pantheon.model.alias.DocumentAlias;
import com.redhat.pantheon.model.document.DocumentVariant;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Renders the alias nodes",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletResourceTypes(
        methods = "GET",
        resourceTypes = "pantheon/alias",
        extensions = {"json", "html"}
)
public class DocumentAliasRenderer extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        DocumentAlias alias = request.getResource().adaptTo(DocumentAlias.class);
        String requestedExt = request.getRequestPathInfo().getExtension();

        DocumentVariant docVariant = null;
        try {
            docVariant = alias.target().getReference();
        } catch (RepositoryException e) {
            throw new ServletException(e);
        }
        if(docVariant == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        else if(requestedExt.equalsIgnoreCase("html")) {
            // FIXME This is doing a lot of backtracking up the tree to get to the parent document
            request.getRequestDispatcher(docVariant.getParent().getParent().getParent().getPath()
                    + ".preview")
                    .forward(request, response);
        }
        else if(requestedExt.equalsIgnoreCase("json")) {
            // TODO these strings should probably go to a constant
            if(docVariant.getResourceType().equals("pantheon/moduleVariant")) {
                request.getRequestDispatcher("/api/module/variant." + requestedExt + "/"
                        + docVariant.uuid().get())
                        .forward(request, response);
            }
            else if(docVariant.getResourceType().equals("pantheon/assemblyVariant")) {
                request.getRequestDispatcher("/api/assembly/variant." + requestedExt + "/"
                        + docVariant.uuid().get())
                        .forward(request, response);
            }
            else {
                throw new ServletException("Unsupported document type: " + docVariant.getResourceType());
            }
        }
        else {
            // TODO This return code makes sense philosophically, but there might be a
            //  better response
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        }
    }
}
