package com.redhat.pantheon.servlet.util;

import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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

        // Then
        assertEquals("value1", suffix.getParam("param1", sc.request()));
        assertEquals("value2", suffix.getParam("param2", sc.request()));
        assertNull(suffix.getParam("nonexisting", sc.request()));
    }

    @Test
    void partialSuffixSegmentParameters() {
        // Given
        SlingPathSuffix suffix = new SlingPathSuffix("/path/{param1}Suffix/other");
        sc.requestPathInfo().setSuffix("/path/valueSuffix/other");

        // When

        // Then
        assertEquals("value", suffix.getParam("param1", sc.request()));
    }

    @Test
    void suffixWithExtraQueryParameters() {
        // Given
        SlingPathSuffix suffix = new SlingPathSuffix("/path/{param1}");
        sc.requestPathInfo().setSuffix("/path/param1Segment?v=1");

        // When

        // Then
        assertEquals("param1Segment", suffix.getParam("param1", sc.request()));
    }

    @Test
    void suffixWithClientHash() {
        // Given
        SlingPathSuffix suffix = new SlingPathSuffix("/path/{param1}");
        sc.requestPathInfo().setSuffix("/path/param1Segment#clientid");

        // When

        // Then
        assertEquals("param1Segment", suffix.getParam("param1", sc.request()));
    }
}
