package com.redhat.pantheon.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ULocale;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Returns a servlet request parameter as a {@link Locale}, or the provided default value if the
     * parameter was not present or if it was impossible to convert the locale code.
     * This method accepts both IETF BCP 47 language tags (e.g. en-us), or locale codes following the java
     * standards (e.g. en_US), hence it will attempt to canonicalize the provided locale value.
     * @param request
     * @param paramName
     * @param defaultValue
     * @return
     * @see ULocale
     */
    public static Locale paramValueAsLocale(final HttpServletRequest request,
                                            final String paramName,
                                            final Locale defaultValue) {
        Locale paramVal = defaultValue;
        String requestParamVal = request.getParameter(paramName);

        try {
            paramVal = ULocale.createCanonical(requestParamVal).toLocale();
        } catch (Exception ex) {
            // do nothing, proceed with the default value
        }

        // there is a chance the locale is null
        return paramVal == null ? defaultValue : paramVal;
    }

    /**
     * Transforms the locale to an IETF BCP 47 language tag, which is a common URL friendly tag.
     * @param locale The locale object to convert
     * @return The appropriate IETF BCP 47 language tag for the provided locale.
     * @see ULocale#toLanguageTag()
     */
    public static String toLanguageTag(Locale locale) {
        return ULocale.forLocale(locale).toLanguageTag().toLowerCase();
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

    /**
     * Utility method to help with path parameter extraction. Servlet filters in Sling may be
     * mapped to regular expression patterns. Since these expressions allow for named groups,
     * the provided matcher allows for extraction of these path sections or path paramters.
     *
     * Example:
     *
     * For a servlet filter path regex = /servlet/(?<param1>[^/]+)/(?<param2>[^/]+)
     * and an actual filter path = /servlet/value1/value2
     *
     * getPathMatcher(regex, request).group("param1") == "value1"
     * getPathMatcher(regex, request).group("param2") == "value2"
     *
     * more complex manipulations may be done depending on the regular expression and use case.
     *
     * @param pathRegexp The Path defining regular expression. This regex ideally should be the same
     *                  one used in the {@link org.apache.sling.servlets.annotations.SlingServletFilter}
     *                  annotation, but it's not necessary. To be useful for extraction it should contain
     *                  either named or ordered groups.
     * @param request The request being operated on. The actual path will be extracted from this request.
     * @return A matcher to extract values from or validate the path of an actual request.
     * @throws {@link RuntimeException} if the request's path does not match the provided regular expression
     */
    public static Matcher getPathMatcher(final String pathRegexp, final HttpServletRequest request) {
        Matcher matcher = Pattern.compile(pathRegexp).matcher(request.getPathInfo());
        if(!matcher.matches()) {
            throw new RuntimeException("Request path: " + request.getPathInfo() + " does not match provided regexp: "
                    + pathRegexp);
        }
        return matcher;
    }

    /**
     * @param input
     * @return
     */
    public static String getHash(String input) {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
