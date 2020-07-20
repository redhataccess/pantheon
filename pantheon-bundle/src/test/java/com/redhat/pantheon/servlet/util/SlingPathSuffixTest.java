package com.redhat.pantheon.servlet.util;

import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Carlos Munoz
 */
@ExtendWith({SlingContextExtension.class})
class SlingPathSuffixTest {

    SlingContext sc = new SlingContext();

    @Test
    void basicParameterTest() {
        // Given
        SlingPathSuffix suffix = new SlingPathSuffix("/path/{param1}/{param2}/other");
        sc.requestPathInfo().setSuffix("/path/value1/value2/other");

        // When
        Map<String, String> params = suffix.getParameters(sc.request());

        // Then
        assertEquals("value1", params.get("param1"));
        assertEquals("value2", params.get("param2"));
        assertFalse(params.containsKey("nonexisting"));
        assertNull(params.get("nonexisting"));
    }

    @Test
    void partialSuffixSegmentParameters() {
        // Given
        SlingPathSuffix suffix = new SlingPathSuffix("/path/{param1}Suffix/other");
        sc.requestPathInfo().setSuffix("/path/valueSuffix/other");

        // When
        Map<String, String> params = suffix.getParameters(sc.request());

        // Then
        assertEquals("value", params.get("param1"));
    }

    @Test
    void suffixWithExtraQueryParameters() {
        // Given
        SlingPathSuffix suffix = new SlingPathSuffix("/path/{param1}");
        sc.requestPathInfo().setSuffix("/path/param1Segment?v=1");

        // When
        Map<String, String> params = suffix.getParameters(sc.request());

        // Then
        assertEquals("param1Segment", params.get("param1"));
    }

    @Test
    void suffixWithClientHash() {
        // Given
        SlingPathSuffix suffix = new SlingPathSuffix("/path/{param1}");
        sc.requestPathInfo().setSuffix("/path/param1Segment#clientid");

        // When
        Map<String, String> params = suffix.getParameters(sc.request());

        // Then
        assertEquals("param1Segment", params.get("param1"));
    }
}
