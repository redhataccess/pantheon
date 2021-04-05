package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.model.assembly.AssemblyVariant;
import com.redhat.pantheon.model.module.ModuleVariant;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.Map;

import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SlingContextExtension.class})
public class VariantJsonServletTest {
    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);
    String testHTML = "<!DOCTYPE html> <html lang=\"en\"> <head><title>test title</title></head> <body " +
            "class=\"article\"><h1>test title</h1> </header> </body> </html>";

    @Test
    void getQuery() {
        ResourceResolver resourceResolver = slingContext.resourceResolver();
        MockSlingHttpServletRequest request = slingContext.request();
        request.setResource(resourceResolver.getResource("/api/module/variant"));
        MockRequestPathInfo requestPathInfo = (MockRequestPathInfo)request.getRequestPathInfo();
        requestPathInfo.setExtension("json");
        requestPathInfo.setSuffix("/123-456-789");

        // Given
        VariantJsonServlet servlet = new VariantJsonServlet();

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
                .resource("/content/repositories/repo/entities/enterprise/module/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:moduleVariant")
                .resource("/content/repositories/repo/entities/enterprise/module/en_US/variants/DEFAULT/draft/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/repo/entities/enterprise/module/en_US/source/draft/jcr:content",
                        "jcr:data", "This is the source content")
                .resource("/content/repositories/repo/entities/enterprise/module/en_US/variants/DEFAULT/draft/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .resource("/content/repositories/repo/entities/enterprise/assemblies/changes/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:assemblyVariant")
                .resource("/content/repositories/repo/entities/enterprise/assemblies/changes/en_US/variants/DEFAULT/draft",
                        "jcr:primaryType", "pant:assemblyVersion")
                .resource("/content/repositories/repo/entities/enterprise/assemblies/changes/en_US/variants/DEFAULT/draft/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/repo/entities/enterprise/assemblies/changes/en_US/variants/DEFAULT/draft/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .resource("/content/repositories/repo/entities/enterprise/assemblies/changes/en_US/variants/DEFAULT/draft/content/0",
                        "jcr:moduleVariantUuid", slingContext.resourceResolver()
                                .getResource("/content/repositories/repo/entities/enterprise/module/en_US/variants/DEFAULT")
                                .getValueMap()
                                .get("jcr:uuid")
                                .toString())
                .commit();

        registerMockAdapter(ModuleVariant.class, slingContext);
        registerMockAdapter(AssemblyVariant.class,slingContext);
        VariantJsonServlet servlet = new VariantJsonServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/repo/entities/enterprise/module/en_US/variants/DEFAULT") );

        // When
        Map<String, Object> map = servlet.resourceToMap(
                slingContext.request(),
                slingContext.resourceResolver().getResource("/content/repositories/repo/entities/enterprise/module/en_US/variants/DEFAULT"));
        Map<String, Object> moduleMap = (Map<String, Object>)map.get("module");

        // Then
        assertTrue(map.containsKey("status"));
        assertTrue(map.containsKey("message"));
        assertTrue(map.containsKey("module"));
        assertTrue(moduleMap.containsKey("uuid"));
        assertTrue(moduleMap.containsKey("products"));
        assertTrue(moduleMap.containsKey("description"));
        assertTrue(moduleMap.containsKey("locale"));
        assertTrue(moduleMap.containsKey("title"));
        assertTrue(moduleMap.containsKey("body"));
        assertTrue(moduleMap.containsKey("content_type"));
        assertTrue(moduleMap.containsKey("date_modified"));
        assertTrue(moduleMap.containsKey("date_published"));
        assertTrue(moduleMap.containsKey("status"));
        assertTrue(moduleMap.containsKey("context_id"));
        assertTrue(moduleMap.containsKey("headline"));
        assertTrue(moduleMap.containsKey("module_url_fragment"));
        assertTrue(moduleMap.containsKey("revision_id"));
        assertTrue(moduleMap.containsKey("context_url_fragment"));
        assertTrue(moduleMap.containsKey("included_in_guides"));
        assertTrue(moduleMap.containsKey("isPartOf"));
        assertEquals((map.get("message")), "Module Found");
        assertEquals((map.get("status")), SC_OK);
    }

    @Test
    @EnabledIf("null != systemEnvironment.get('PORTAL_URL')")
    public void onlyRenderViewURIForPORTAL() throws RepositoryException {
        // Given
        slingContext.build()
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:moduleVariant")
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/draft/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/repo/module/en_US/source/draft/jcr:content",
                        "jcr:data", "This is the source content")
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/draft/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .commit();

        registerMockAdapter(ModuleVariant.class, slingContext);
        VariantJsonServlet servlet = new VariantJsonServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/variants/DEFAULT") );

        // When
        Map<String, Object> map = servlet.resourceToMap(
                slingContext.request(),
                slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/variants/DEFAULT"));
        Map<String, Object> moduleMap = (Map<String, Object>)map.get("map");

        assertTrue(moduleMap.containsKey("view_uri"));
    }
}
