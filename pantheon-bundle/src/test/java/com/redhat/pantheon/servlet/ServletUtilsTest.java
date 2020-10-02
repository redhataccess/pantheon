package com.redhat.pantheon.servlet;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServletUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void paramValue() {
        // Given
        lenient().when(request.getParameter(eq("stringParam"))).thenReturn("stringValue");
        lenient().when(request.getParameter(eq("emptyStringParam"))).thenReturn("");
        lenient().when(request.getParameter(eq("nullStringParam"))).thenReturn(null);

        // When

        // Then
        assertEquals("stringValue", ServletUtils.paramValue(request, "stringParam"));
        assertEquals("", ServletUtils.paramValue(request, "emptyStringParam"));
        assertEquals(null, ServletUtils.paramValue(request, "nullStringParam"));
    }

    @Test
    void paramValueWithDefault() {
        // Given
        final String defaultValue = "__DEFAULT__";
        lenient().when(request.getParameter(eq("stringParam"))).thenReturn("stringValue");
        lenient().when(request.getParameter(eq("emptyStringParam"))).thenReturn("");
        lenient().when(request.getParameter(eq("nullStringParam"))).thenReturn(null);

        // When

        // Then
        assertEquals("stringValue", ServletUtils.paramValue(request, "stringParam", defaultValue));
        assertEquals("", ServletUtils.paramValue(request, "emptyStringParam", defaultValue));
        assertEquals(defaultValue, ServletUtils.paramValue(request, "nullStringParam", defaultValue));
    }

    @Test
    void paramValueAsLong() {
        // Given
        lenient().when(request.getParameter(eq("longParam"))).thenReturn("15");
        lenient().when(request.getParameter(eq("emptyLongParam"))).thenReturn("");
        lenient().when(request.getParameter(eq("nullLongParam"))).thenReturn(null);
        lenient().when(request.getParameter(eq("unparseableParam"))).thenReturn("bdobfospc");
        lenient().when(request.getParameter(eq("negativeParam"))).thenReturn("-15");

        // When

        // Then
        assertEquals(new Long(15), ServletUtils.paramValueAsLong(request, "longParam"));
        assertEquals(null, ServletUtils.paramValueAsLong(request, "emptyLongParam"));
        assertEquals(null, ServletUtils.paramValueAsLong(request, "nullLongParam"));
        assertEquals(null, ServletUtils.paramValueAsLong(request, "unparseableParam"));
        assertEquals(new Long(-15), ServletUtils.paramValueAsLong(request, "negativeParam"));
    }

    @Test
    void paramValueAsLongWithDefault() {
        // Given
        lenient().when(request.getParameter(eq("longParam"))).thenReturn("15");
        lenient().when(request.getParameter(eq("emptyLongParam"))).thenReturn("");
        lenient().when(request.getParameter(eq("nullLongParam"))).thenReturn(null);
        lenient().when(request.getParameter(eq("unparseableParam"))).thenReturn("bdobfospc");
        lenient().when(request.getParameter(eq("negativeParam"))).thenReturn("-15");

        // When

        // Then
        assertEquals(new Long(15), ServletUtils.paramValueAsLong(request, "longParam", 20L));
        assertEquals(new Long(20), ServletUtils.paramValueAsLong(request, "emptyLongParam", 20L));
        assertEquals(new Long(20), ServletUtils.paramValueAsLong(request, "nullLongParam", 20L));
        assertEquals(new Long(20), ServletUtils.paramValueAsLong(request, "unparseableParam", 20L));
        assertEquals(new Long(-15), ServletUtils.paramValueAsLong(request, "negativeParam", 20L));
    }

    @Test
    void paramValueAsBoolean() {
        // Given
        lenient().when(request.getParameter(eq("booleanParam"))).thenReturn("true");
        lenient().when(request.getParameter(eq("emptyParam"))).thenReturn("");
        lenient().when(request.getParameter(eq("nullParam"))).thenReturn(null);
        lenient().when(request.getParameter(eq("yesParam"))).thenReturn("yes");
        lenient().when(request.getParameter(eq("caseSensitiveParam"))).thenReturn("tRuE");
        lenient().when(request.getParameter(eq("numericParam"))).thenReturn("1");

        // When

        // Then
        Assertions.assertTrue(ServletUtils.paramValueAsBoolean(request, "booleanParam"));
        Assertions.assertTrue(ServletUtils.paramValueAsBoolean(request, "caseSensitiveParam"));

        Assertions.assertFalse(ServletUtils.paramValueAsBoolean(request, "emptyParam"));
        Assertions.assertFalse(ServletUtils.paramValueAsBoolean(request, "nullParam"));
        Assertions.assertFalse(ServletUtils.paramValueAsBoolean(request, "yesParam"));
        Assertions.assertFalse(ServletUtils.paramValueAsBoolean(request, "numericParam"));
    }

    @Test
    void writeAsJson() throws Exception {
        // Given
        response = spy(response);
        PrintWriter w = mock(PrintWriter.class);
        lenient().when(response.getWriter()).thenReturn(w);
        Map obj = newHashMap();
        obj.put("name", "a-value");
        obj.put("number", 10);

        // When
        ServletUtils.writeAsJson(response, obj);

        // Then
        verify(response).setContentType("application/json");
        verify(response).getWriter();
    }

    @Test
    void paramValueAsLocale() {
        // Given
        lenient().when(request.getParameter(eq("locale"))).thenReturn("en_US");
        lenient().when(request.getParameter(eq("locale2"))).thenReturn("en-us");
        lenient().when(request.getParameter(eq("locale3"))).thenReturn("fr-fr");
        lenient().when(request.getParameter(eq("locale4"))).thenReturn("fr_FR");

        // When

        // Then
        assertEquals(Locale.US, ServletUtils.paramValueAsLocale(request, "locale", null));
        assertEquals(Locale.US, ServletUtils.paramValueAsLocale(request, "locale2", null));
        assertEquals(Locale.FRANCE, ServletUtils.paramValueAsLocale(request, "locale3", null));
        assertEquals(Locale.FRANCE, ServletUtils.paramValueAsLocale(request, "locale4", null));
        assertNull(ServletUtils.paramValueAsLocale(request, "nonExistentParameter", null));
        assertEquals(Locale.FRANCE, ServletUtils.paramValueAsLocale(request, "nonExistentParameter", Locale.FRANCE));
    }

    @Test
    void getPathMatcher() {
        // Given
        final String pathRegexp = "/prefix/(?<param1>[^/]+)/(?<param2>[^/]+)";
        when(request.getPathInfo()).thenReturn("/prefix/value1/value2");

        // When
        Matcher pathMatcher = ServletUtils.getPathMatcher(pathRegexp, request);

        // Then
        assertEquals("value1", pathMatcher.group("param1"));
        assertEquals("value1", pathMatcher.group(1));
        assertEquals("value2", pathMatcher.group("param2"));
        assertEquals("value2", pathMatcher.group(2));
    }

    @Test
    void getInvalidPathMatcher() {
        // Given
        final String pathRegexp = "/prefix/(?<param1>[^/]+)/(?<param2>[^/]+)";
        when(request.getPathInfo()).thenReturn("/prefix/value1/value2/value3");

        // When

        // Then
        assertThrows(RuntimeException.class, () -> ServletUtils.getPathMatcher(pathRegexp, request));
    }

    @Test
    void toLanguageTag() {
        assertEquals("en-us", ServletUtils.toLanguageTag(Locale.US));
        assertEquals("fr", ServletUtils.toLanguageTag(Locale.FRENCH));
        assertEquals("ja-jp", ServletUtils.toLanguageTag(Locale.JAPAN));
        assertEquals("es", ServletUtils.toLanguageTag(new Locale("es")));
    }

    private String readInputStream(InputStream is, Charset encoding) {
        try {
            return IOUtils.toString(is, encoding).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest(name = "streaming param with value ''{0}'' and charset {1}")
    @ArgumentsSource(EncodingArgsProvider.class)
    void handleParamWithEncoding(String string, Charset encoding) throws Exception {
        // Given
        SlingHttpServletRequest slingReq = mock(SlingHttpServletRequest.class);
        RequestParameter rp = mock(RequestParameter.class);
        when(rp.getInputStream()).thenReturn(new ByteArrayInputStream(encoding.encode(string).array()));
        when(slingReq.getRequestParameter("str")).thenReturn(rp);

        // When

        // Then
        assertEquals(string,
                ServletUtils.handleParamAsStream(slingReq, "str", encoding.name(), is -> readInputStream(is, encoding))
        );
    }

    public static class EncodingArgsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of("Entwickeln Sie mit Vergnügen", StandardCharsets.UTF_8),
                    Arguments.of("abc+<>@", StandardCharsets.ISO_8859_1),
                    Arguments.of("私の犬は私の宿題を食べました", StandardCharsets.UTF_8)
            );
        }
    }
}