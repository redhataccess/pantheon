package com.redhat.pantheon.jcr;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class JcrResourcesTest {
    private final SlingContext sc = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void rename() throws Exception{
        // Given
        sc.build()
                .resource("/content/repo/original",
                        "name", "test name")
                .commit();
        // When
        JcrResources.rename(sc.resourceResolver().getResource("/content/repo/original"), "new");

        // Then
        assertNull(sc.resourceResolver().getResource("/content/repo/original"));
        assertNotNull(sc.resourceResolver().getResource("/content/repo/new"));
        assertEquals("test name", sc.resourceResolver().getResource("/content/repo/new").getValueMap().get("name"));
    }
}