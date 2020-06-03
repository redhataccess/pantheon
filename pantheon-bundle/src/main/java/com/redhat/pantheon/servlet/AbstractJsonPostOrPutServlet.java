package com.redhat.pantheon.servlet;

import com.redhat.pantheon.helper.TransformToPojo;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Abstract servlet class to provide common behaviour that accepts a json payload in the body of a POST or PUT
 * web request, parses said payload into a java class, and then operates on the object. All the json
 * deserialization logic is encapsulated in this class.
 *
 * Implementations should override the processPost of processPut methods (both are allowed) to implement
 * specific logic for either case.
 *
 * @author Carlos Munoz
 */
public class AbstractJsonPostOrPutServlet<T> extends SlingAllMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(AbstractJsonPostOrPutServlet.class);
    private final Class<T> jsonType;

    public AbstractJsonPostOrPutServlet(Class<T> jsonType) {
        this.jsonType = jsonType;
    }

    @Override
    protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        TransformToPojo transformToPojo = new TransformToPojo();
        T jsonPayload = transformToPojo.fromJson(jsonType, request.getReader());

        try {
            processPost(request, response, jsonPayload);
        } catch (Exception e) {
            log.error("Error processing POST operation", e);
            throw new ServletException(e);
        }
    }

    @Override
    protected final void doPut(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        TransformToPojo transformToPojo = new TransformToPojo();
        T jsonPayload = transformToPojo.fromJson(jsonType, request.getReader());

        try {
            processPut(request, response, jsonPayload);
        } catch (Exception e) {
            log.error("Error processing PUT operation", e);
            throw new ServletException(e);
        }
    }

    /**
     * Implements the logic in the event of a POST invocation. By default it simply sends a 405 response.
     * @param request The servlet request. For parameter and path parameter extractions.
     * @param response The servlet response. For custom error or response codes and messages.
     * @param jsonPayload The parsed body of the request into a java class
     * @throws Exception If there is a problem processing the request.
     */
    protected void processPost(final SlingHttpServletRequest request,
                               final SlingHttpServletResponse response,
                               final T jsonPayload)
            throws Exception {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Method POST is not allowed for " + request.getPathInfo());
    }

    /**
     * Implements the logic in the event of a PUT invocation. By default it simply sends a 405 response.
     * @param request The servlet request. For parameter and path parameter extractions.
     * @param response The servlet response. For custom error or response codes and messages.
     * @param jsonPayload The parsed body of the request into a java class
     * @throws Exception If there is a problem processing the request.
     */
    protected void processPut(final SlingHttpServletRequest request,
                              final SlingHttpServletResponse response,
                              final T jsonPayload)
            throws Exception {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Method PUT is not allowed for " + request.getPathInfo());
    }
}
