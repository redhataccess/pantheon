package com.redhat.pantheon.servlet.assembly;

import com.redhat.pantheon.model.assembly.AssemblyPage;
import com.redhat.pantheon.model.assembly.AssemblyVariant;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.workspace.Workspace;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.jcr.query.Query;
import java.util.List;
import java.util.Map;

import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SlingContextExtension.class})
public class AssemblyIncludesServletTest {
    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);
    String testHTML = "<!DOCTYPE html> <html lang=\"en\"> <head><title>test title</title></head> <body " +
            "class=\"article\"><h1>test title</h1> </header> </body> </html>";

    @Test
    void getQuery() {
        ResourceResolver resourceResolver = slingContext.resourceResolver();
        MockSlingHttpServletRequest request = slingContext.request();
        request.setResource(resourceResolver.getResource("/api/assembly/variant"));
        MockRequestPathInfo requestPathInfo = (MockRequestPathInfo)request.getRequestPathInfo();
        requestPathInfo.setExtension("json");
        requestPathInfo.setSuffix("/123-456-789");

        // Given
        AssemblyIncludesServlet servlet = new AssemblyIncludesServlet();

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
                .resource("/content/repositories/rhel-8-docs",
                        "jcr:primaryType", "pant:workspace")
                .resource("/content/repositories/rhel-8-docs/entities/modules/name",
                        "jcr:primaryType", "pant:module")
                .resource("/content/repositories/rhel-8-docs/entities/modules/name/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:moduleVariant")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT",
                        "jcr:primaryType", "pant:assemblyVariant")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft",
                        "jcr:primaryType", "pant:assemblyVersion")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description")
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/cached_html/jcr:content",
                        "jcr:data", testHTML)
                .commit();
        String moduleUuid = (String) slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/modules/name").getValueMap().get("jcr:uuid");
        slingContext.create()
                .resource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT/draft/content/0",
                        "pant:moduleUuid", moduleUuid);

        registerMockAdapter(AssemblyVariant.class, slingContext);
        registerMockAdapter(AssemblyPage.class, slingContext);
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(Workspace.class, slingContext);
        AssemblyIncludesServlet servlet = new AssemblyIncludesServlet();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT") );

        // When
        Map<String, Object> map = servlet.resourceToMap(
                slingContext.request(),
                slingContext.resourceResolver().getResource("/content/repositories/rhel-8-docs/entities/assemblies/changes/en_US/variants/DEFAULT"));
        Map<String, Object> includesMap = (Map<String, Object>)map.get("includes");

        List<Map<String, String>> documents = (List<Map<String, String>>) includesMap.get("documents");
        // Then
        assertTrue(map.containsKey("status"));
        assertTrue(map.containsKey("includes"));

        assertTrue(includesMap.containsKey("document_count"));
        assertTrue(includesMap.containsKey("documents"));

        assertTrue(documents.stream().allMatch(value -> value.containsKey("canonical_uuid")));
        assertTrue(documents.stream().allMatch(value -> value.containsKey("path")));
        assertTrue(documents.stream().allMatch(value -> value.containsKey("title")));
        assertFalse(documents.stream().allMatch(value -> value.containsKey("bad_value")));
        assertEquals((map.get("status")), SC_OK);
    }
}
