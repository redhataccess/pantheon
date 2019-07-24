package com.redhat.pantheon.servlet;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.jcr.query.Query;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({SlingContextExtension.class})
class ModuleListingServletTest {

    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_MOCK);

    @Test
    void getQueryNoParams() {
        // Given
        ModuleListingServlet servlet = new ModuleListingServlet();

        // When
        String query = servlet.getQuery(slingContext.request());

        // Then
        // make sure queries don't throw exceptions when executed against the JCR repository
        assertDoesNotThrow(() -> slingContext.resourceResolver().queryResources(query, Query.JCR_SQL2));
    }

    @Test
    void getQuery() {
        // Given
        ModuleListingServlet servlet = new ModuleListingServlet();
        Map<String, Object> map = newHashMap();
        map.put("search", "search terms");
        map.put("key", "jcr:title");
        map.put("direction", "asc");
        slingContext.request().setParameterMap(map);

        // When
        String query = servlet.getQuery(slingContext.request());

        // Then
        // make sure queries don't throw exceptions when executed against the JCR repository
        assertDoesNotThrow(() -> slingContext.resourceResolver().queryResources(query, Query.JCR_SQL2));
    }

    @Test
    void resourceToMap() {
        // Given

        slingContext.build()
                .resource("/content/repositories/repo/module")
                .commit();

        ModuleListingServlet servlet = new ModuleListingServlet();

        // When
        Map<String, Object> map = servlet.resourceToMap(
                slingContext.resourceResolver().getResource("/content/repositories/repo/module"));

        // Then
        assertTrue(map.containsKey("name"));
        assertTrue(map.containsKey("pant:transientPath"));
        assertTrue(map.containsKey("pant:transientSource"));
        assertTrue(map.containsKey("pant:transientSource"));
    }
}