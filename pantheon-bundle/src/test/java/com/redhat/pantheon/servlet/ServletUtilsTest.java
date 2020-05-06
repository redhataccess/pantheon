package com.redhat.pantheon.servlet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @Test
    void getHash() {
        // Given
        final String input = "Valid Hash";
        String hash = ServletUtils.getHash(input);
        assertEquals("59484ec883ab80b160c240003a250bb0cc03008d734103ce3226297936b116b689cfd335faea804602fcb02e074afc9779a8bff6675ddf4c5184bdc0c9368d84", hash);
    }

    @Test
    void getInvalidHash() {
        // Given
        final String input = null;
        assertThrows(RuntimeException.class, () -> ServletUtils.getHash(input));
    }
}