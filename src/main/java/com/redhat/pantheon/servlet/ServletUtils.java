package com.redhat.pantheon.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * A set of utilities when working with servlets.
 * This is a final no constructor class which only provides services.
 */
public final class ServletUtils {

    private ServletUtils() {
    }

    /**
     * Returns a string servlet request parameter, or the provided default value if the parameter was not present.
     * @param request
     * @param paramName
     * @param defaultValue
     * @return
     */
    public static @Nullable String paramValue(final HttpServletRequest request, final String paramName,
                      final String defaultValue) {
        String paramVal = defaultValue;
        String requestParamVal = request.getParameter(paramName);

        if(requestParamVal != null) {
            paramVal = requestParamVal;
        }
        return paramVal;
    }

    /**
     * Returns a string servlet request parameter, or null if the parameter was not present.
     * @param request
     * @param paramName
     * @return
     */
    public static @Nullable
    String paramValue(final HttpServletRequest request, final String paramName) {
        return paramValue(request, paramName, null);
    }

    /**
     * Returns a servlet request parameter as a Long, or the provided default value if the parameter was not present or
     * unparseable to a Long value.
     * @param request
     * @param paramName
     * @param defaultValue
     * @return
     */
    public static @Nullable Long paramValueAsLong(final HttpServletRequest request, final String paramName,
                                             final Long defaultValue) {
        Long paramVal = defaultValue;
        String requestParamVal = request.getParameter(paramName);

        try {
            paramVal = Long.parseLong(requestParamVal);
        } catch (NumberFormatException nfx) {
            // Do nothing, silently continue with the default value
        }
        return paramVal;
    }

    /**
     * Returns a servlet request parameter as a Long, or null if the parameter was not present or
     * unparseable to a Long value.
     * @param request
     * @param paramName
     * @return
     */
    public static @Nullable
    Long paramValueAsLong(final HttpServletRequest request, final String paramName) {
        return paramValueAsLong(request, paramName, null);
    }

    /**
     * Returns a servlet request parameter as a boolean. This method will only return true if the
     * parameter value is the string 'True' (not case-sensitive)
     * @param request
     * @param paramName
     * @return
     */
    public static
    boolean paramValueAsBoolean(final HttpServletRequest request, final String paramName) {
        return Boolean.parseBoolean(request.getParameter(paramName));
    }

    /**
     * Writes the given payload as a json string to the servlet response.
     * @param response The servlet response on which to write
     * @param payload The object to turn to json and write
     * @throws IOException If there is a problem writing to the servlet response
     */
    public static void writeAsJson(final HttpServletResponse response, final Object payload) throws IOException {
        response.setContentType("application/json");
        Writer w = response.getWriter();
        w.write(new ObjectMapper().writer().writeValueAsString(payload));
    }
}
