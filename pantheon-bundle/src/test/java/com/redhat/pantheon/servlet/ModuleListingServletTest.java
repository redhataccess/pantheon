package com.redhat.pantheon.servlet;

import com.redhat.pantheon.model.module.Module;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.jcr.query.Query;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({SlingContextExtension.class})
class ModuleListingServletTest {

    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void getQueryNoParams() {
        // Given
        ModuleListingServlet servlet = new ModuleListingServlet();

        // When
        String query = servlet.getQuery(slingContext.request());

        // Then
        // make sure queries don't throw exceptions when executed against the JCR repository
        assertDoesNotThrow(() -> slingContext.resourceResolver().queryResources(query, Query.XPATH));
    }

    @Test
    void getQuery() {
        // Given
        ModuleListingServlet servlet = new ModuleListingServlet();
        Map<String, Object> map = newHashMap();
        map.put("search", "search terms");
        map.put("key", "Updated");
        map.put("direction", "asc");
        map.put("productversion", new String[]{"id1", "id2", "id3"});
        map.put("product", new String[]{"prod1", "prod2", "prod3"});
        map.put("type", "Usecase");
        slingContext.request().setParameterMap(map);

        // When
        String query = servlet.getQuery(slingContext.request());

        // Then
        // make sure queries don't throw exceptions when executed against the JCR repository
        assertDoesNotThrow(() -> slingContext.resourceResolver().queryResources(query, Query.XPATH));
    }

    @Test
    void resourceToMap() {
        // Given
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/1",
                        "jcr:primaryType", "pant:moduleVersion");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/1/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description");
        slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US").adaptTo(ModifiableValueMap.class)
                .put("draft", slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/1").getValueMap()
                        .get("jcr:uuid"));
        registerMockAdapter(Module.class, slingContext);
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