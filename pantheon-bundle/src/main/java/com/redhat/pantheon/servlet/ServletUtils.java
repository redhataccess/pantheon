package com.redhat.pantheon.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ULocale;
import com.redhat.pantheon.extension.url.CustomerPortalUrlUuidProvider;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.servlet.util.ServletHelper;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.http.HttpStatus;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.servlets.post.PostResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Locale;
import java.util.function.Function;
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
     * Performs a function over the contents of a request parameter using an {@link InputStream}.
     * The InputStream is created, processed and then discarded internally. This particular method
     * parses the content as a character stream using the provided character set encoding.
     * @param request The Sling servlet request.
     * @param paramName The name of the parameter to extract from the servlet request.
     * @param charsetEncoding The character encoding to use when parsing the stream.
     * @param handler The handler function. Whatever this function returns is also returned by this
     *                method.
     * @param <R>
     * @return The result of processing the stream's content.
     * @throws IOException
     */
    public static <R> R handleParamAsStream(@Nonnull final SlingHttpServletRequest request,
                                            @Nonnull final String paramName,
                                            @Nonnull final String charsetEncoding,
                                            @Nonnull final Function<InputStream, R> handler)
            throws IOException {
        ReaderInputStream ris;
        try (InputStream stream = request.getRequestParameter(paramName).getInputStream()) {
            ris = new ReaderInputStream(new InputStreamReader(stream), charsetEncoding);
            return handler.apply(ris);
        }
    }

    /**
     * Performs a function over the contents of a request parameter using an {@link InputStream}.
     * The InputStream is created, processed and then discarded internally.
     * @param request The Sling servlet request.
     * @param paramName The name of the parameter to extract from the servlet request.
     * @param handler The handler function. Whatever this function returns is also returned by this
     *                method.
     * @param <R>
     * @return The result of processing the stream's content.
     * @throws IOException
     */
    public static <R> R handleParamAsStream(@Nonnull final SlingHttpServletRequest request,
                                            @Nonnull final String paramName,
                                            @Nonnull final Function<InputStream, R> handler)
            throws IOException {
        try (InputStream stream = request.getRequestParameter(paramName).getInputStream()) {
            return handler.apply(stream);
        }
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
     * Transforms the locale to an IETF BCP 47 language tag, which is a common URL friendly tag.
     * @param locale A string representing the locale
     * @return The appropriate IETF BCP 47 language tag for the provided locale.
     * @see ULocale#toLanguageTag()
     */
    public static String toLanguageTag(String locale) {
        return ServletUtils.toLanguageTag(ULocale.createCanonical(locale).toLocale());
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
     * refactored method call to generate customer portalurl
     *
     * @param request
     * @param response
     */

    protected static void getCustomerPortalUrl(SlingHttpServletRequest request, PostResponse response) {
        try {
            Object o = ServletHelper.resourceToModel(request.getResource());
            DocumentVariant dv = o instanceof DocumentVariant
                    ? (DocumentVariant) o
                    : ((Document) o).locale("en_US").get()
                    .variants().get()
                    .canonicalVariant().get();
            String url = new CustomerPortalUrlUuidProvider().generateUrlString(dv);
            response.setStatus(HttpStatus.SC_OK,"{\"url\":\""+url+"\"}");
            response.setPath(url);
        }catch (Exception e){
            throw new RuntimeException("Cannot generate customer portal url: " + request.getResource().getPath());
        }
    }
}
