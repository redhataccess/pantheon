package com.redhat.pantheon.servlet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServletUtilsTest {

    @Mock
    private HttpServletRequest request;

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
}