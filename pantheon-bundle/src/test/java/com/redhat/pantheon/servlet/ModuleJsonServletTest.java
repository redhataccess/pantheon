package com.redhat.pantheon.servlet;

import com.redhat.pantheon.model.module.Module;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.jcr.query.Query;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SlingContextExtension.class})
class ModuleJsonServletTest {

    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);
    String testHTML = "<!DOCTYPE html> <html lang=\"en\"> <head><title>test title</title></head> <body " +
            "class=\"article\"><h1>test title</h1> </header> </body> </html>";

    @Test
    void getQueryNoParams() {
        // Given
        ModuleJsonServlet servlet = new ModuleJsonServlet();

        // When
        String query = servlet.getQuery(slingContext.request());

        // Then
        // make sure queries don't throw exceptions when executed against the JCR repository
        assertDoesNotThrow(() -> slingContext.resourceResolver().queryResources(query, Query.JCR_SQL2));
    }

    @Test
    void getQuery() {
        // Given
        ModuleJsonServlet servlet = new ModuleJsonServlet();
        Map<String, Object> map = newHashMap();
        map.put("locale", DEFAULT_MODULE_LOCALE.toString());
        map.put("module_id", "jcr:uuid");
        slingContext.request().setParameterMap(map);

        // When
        String query = servlet.getQuery(slingContext.request());

        // Then
        // make sure queries don't throw exceptions when executed against the JCR repository
        assertDoesNotThrow(() -> slingContext.resourceResolver().queryResources(query, Query.JCR_SQL2));

        // make sure query response is not null
        assertNotNull(slingContext.response());
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
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/1/content/asciidoc",
                        "jcr:primaryType", "nt:file");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/1/content/cachedHtml",
                        "jcr:data", testHTML,
                        "pant:hash", "2a0e2c43");
        slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US").adaptTo(ModifiableValueMap.class)
                .put("released", slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/1").getValueMap()
                        .get("jcr:uuid"));

        registerMockAdapter(Module.class, slingContext);
        ModuleJsonServlet servlet = new ModuleJsonServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/module") );

        // When
        Map<String, Object> map = servlet.resourceToMap(
                slingContext.resourceResolver().getResource("/content/repositories/repo/module"));
        Map<String, Object> moduleMap = (Map<String, Object>)map.get("module");

        // Then
        assertTrue(map.containsKey("status"));
        assertTrue(map.containsKey("message"));
        assertTrue(map.containsKey("module"));
        assertTrue(moduleMap.containsKey("module_uuid"));
        assertTrue(moduleMap.containsKey("product_version"));
        assertTrue(moduleMap.containsKey("description"));
        assertTrue(moduleMap.containsKey("locale"));
        assertTrue(moduleMap.containsKey("title"));
        assertTrue(moduleMap.containsKey("body"));
        assertTrue(moduleMap.containsKey("product_name"));
        assertTrue(moduleMap.containsKey("content_type"));
        assertTrue(moduleMap.containsKey("date_modified"));
        assertTrue(moduleMap.containsKey("date_published"));
        assertTrue(moduleMap.containsKey("context_id"));
        assertTrue(moduleMap.containsKey("headline"));
        assertTrue(moduleMap.containsKey("module_url_fragment"));
        assertTrue(moduleMap.containsKey("revision_id"));
        assertTrue(moduleMap.containsKey("context_url_fragment"));
        assertEquals((map.get("message")), "Module Found");
        assertEquals((map.get("status")), SC_OK);
    }
}