package com.redhat.pantheon.servlet;

import com.redhat.pantheon.jcr.JcrQueryHelper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.servlet.ServletUtils.writeAsJson;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;


/**
 * Servlet that helps create simple Json result from custom queries.
 * The servlet returns the first object from the query response irrespective
 * of the number of elements returned in the response.
 * This servlet should be extended in the case where you need only the 1st/single element
 * from the result in json format. See methods to be overriden for details on how to
 * customize sub classes.
 *
 * @author Ankit Gadgil
 */
public abstract class AbstractJsonSingleQueryServlet extends SlingSafeMethodsServlet {

    private String customErrorMessage = null;

    protected void setCustomErrorMessage(String message) {
        customErrorMessage = message;
    }

    protected String getCustomErrorMessage(String defaultMessage) {
        return Optional.ofNullable(customErrorMessage).orElse(defaultMessage);
    }

    /**
     * Returns the query to execute. The query may be modified depending on the provided
     * parameters in the request.
     * @param request The servlet request
     * @return A string with the query to execute. The results of this query will be used
     * to render the list of results.
     */
    protected abstract String getQuery(SlingHttpServletRequest request);

    /**
     * Provides a way to modify the returned objects based on the found resources.
     * The default implementation just returns the corresponding value map.
     * @param request The servlet request
     * @param resource The Resource obtained as a result of the query.
     * @return A map with the actual value to be returned to the servlet's caller.
     * @throws RepositoryException
     */
    protected Map<String, Object> resourceToMap(@Nonnull SlingHttpServletRequest request,
                                                @NotNull Resource resource) throws RepositoryException {
        return newHashMap(resource.getValueMap());
    }

    /**
     * A post-processor that provides a way to add filter logic for returned objects
     * The default implementation returns true and can be overriden to check for conditions
     * @param request The servlet request
     * @param resource The Resource obtained as a result of the query.
     * @return Boolean true.
    */
    protected boolean isValidResource(@Nonnull SlingHttpServletRequest request, @Nonnull Resource resource) {
        return true;
    }

    @Override
    protected final void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response)
            throws ServletException, IOException {

        JcrQueryHelper queryHelper = new JcrQueryHelper(request.getResourceResolver());
        try {
            Stream<Resource> resultStream = queryHelper.query(getQuery(request));

            Optional<Resource> firstResource = resultStream.findFirst();
            if(firstResource.isPresent()) {
                if(isValidResource(request, firstResource.get())) {
                    // Convert the resource to JSON
                    writeAsJson(response, resourceToMap(request, firstResource.get()));
                }
                else {
                    response.sendError(SC_NOT_FOUND, getCustomErrorMessage("Requested resource was invalid."));
                }
            } else {
                response.sendError(SC_NOT_FOUND, getCustomErrorMessage("Requested content not found."));
            }

        } catch (RepositoryException e) {
            throw new ServletException(e);
        }
    }

}
