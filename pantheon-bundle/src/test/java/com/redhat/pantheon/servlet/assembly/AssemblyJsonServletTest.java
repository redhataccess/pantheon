package com.redhat.pantheon.servlet.assembly;

import com.redhat.pantheon.model.assembly.Assembly;
import com.redhat.pantheon.servlet.ServletUtils;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SlingContextExtension.class})
class AssemblyJsonServletTest {

    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);
    String testHTML = "<!DOCTYPE html> <html lang=\"en\"> <head><title>test title</title></head> <body " +
            "class=\"article\"><h1>test title</h1> </header> </body> </html>";

    @Test
    void getQueryNoParams() {
        // Given
        AssemblyJsonServlet servlet = new AssemblyJsonServlet();

        // When
        String query = servlet.getQuery(slingContext.request());

        // Then
        // make sure queries don't throw exceptions when executed against the JCR repository
        assertDoesNotThrow(() -> slingContext.resourceResolver().queryResources(query, Query.JCR_SQL2));
    }

    @Test
    void getQuery() {
        // Given
        AssemblyJsonServlet servlet = new AssemblyJsonServlet();
        Map<String, Object> map = newHashMap();
        map.put("locale", ServletUtils.toLanguageTag(Locale.US));
        map.put("assembly_id", "jcr:uuid");
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
    void resourceToMap() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes",
                        "jcr:primaryType", "pant:assembly")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/jcr:content",
                        "jcr:data", "This is the source content")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .commit();

        registerMockAdapter(Assembly.class, slingContext);
        AssemblyJsonServlet servlet = new AssemblyJsonServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes") );

        // When
        Map<String, Object> map = servlet.resourceToMap(
                slingContext.request(),
                slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes"));
        Map<String, Object> assemblyMap = (Map<String, Object>)map.get("assembly");

        // Then
        assertTrue(map.containsKey("status"));
        assertTrue(map.containsKey("message"));
        assertTrue(map.containsKey("assembly"));
        assertTrue(assemblyMap.containsKey("assembly_uuid"));
        assertTrue(assemblyMap.containsKey("products"));
        assertTrue(assemblyMap.containsKey("locale"));
        assertTrue(assemblyMap.containsKey("title"));
        assertTrue(assemblyMap.containsKey("body"));
        assertTrue(assemblyMap.containsKey("content_type"));
        assertTrue(assemblyMap.containsKey("date_modified"));
        assertTrue(assemblyMap.containsKey("date_published"));
        assertTrue(assemblyMap.containsKey("status"));
        assertTrue(assemblyMap.containsKey("context_id"));
        assertTrue(assemblyMap.containsKey("headline"));
        assertTrue(assemblyMap.containsKey("assembly_url_fragment"));
        assertTrue(assemblyMap.containsKey("revision_id"));
        assertTrue(assemblyMap.containsKey("context_url_fragment"));
        assertEquals((map.get("message")), "Assembly Found");
        assertEquals((map.get("status")), SC_OK);
    }

    @Test
    @EnabledIf("null != systemEnvironment.get('PORTAL_URL')")
    public void onlyRenderViewURIForPORTAL() throws RepositoryException {
        // Given
        slingContext.build()
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes",
                        "jcr:primaryType", "pant:assembly")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/jcr:content",
                        "jcr:data", "This is the source content")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/released/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .commit();

        registerMockAdapter(Assembly.class, slingContext);
        AssemblyJsonServlet servlet = new AssemblyJsonServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes") );

        // When
        Map<String, Object> map = servlet.resourceToMap(
                slingContext.request(),
                slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes"));
        Map<String, Object> assemblyMap = (Map<String, Object>)map.get("assembly");

        //Then
        assertTrue(assemblyMap.containsKey("view_uri"));
    }
}