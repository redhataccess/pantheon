package com.redhat.pantheon.servlet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
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
        Assertions.assertEquals("stringValue", ServletUtils.paramValue(request, "stringParam"));
        Assertions.assertEquals("", ServletUtils.paramValue(request, "emptyStringParam"));
        Assertions.assertEquals(null, ServletUtils.paramValue(request, "nullStringParam"));
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
        Assertions.assertEquals("stringValue", ServletUtils.paramValue(request, "stringParam", defaultValue));
        Assertions.assertEquals("", ServletUtils.paramValue(request, "emptyStringParam", defaultValue));
        Assertions.assertEquals(defaultValue, ServletUtils.paramValue(request, "nullStringParam", defaultValue));
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
        Assertions.assertEquals(new Long(15), ServletUtils.paramValueAsLong(request, "longParam"));
        Assertions.assertEquals(null, ServletUtils.paramValueAsLong(request, "emptyLongParam"));
        Assertions.assertEquals(null, ServletUtils.paramValueAsLong(request, "nullLongParam"));
        Assertions.assertEquals(null, ServletUtils.paramValueAsLong(request, "unparseableParam"));
        Assertions.assertEquals(new Long(-15), ServletUtils.paramValueAsLong(request, "negativeParam"));
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
        Assertions.assertEquals(new Long(15), ServletUtils.paramValueAsLong(request, "longParam", 20L));
        Assertions.assertEquals(new Long(20), ServletUtils.paramValueAsLong(request, "emptyLongParam", 20L));
        Assertions.assertEquals(new Long(20), ServletUtils.paramValueAsLong(request, "nullLongParam", 20L));
        Assertions.assertEquals(new Long(20), ServletUtils.paramValueAsLong(request, "unparseableParam", 20L));
        Assertions.assertEquals(new Long(-15), ServletUtils.paramValueAsLong(request, "negativeParam", 20L));
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
}