package com.redhat.pantheon.servlet.module;

import com.google.common.collect.Maps;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.util.TestUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SlingContextExtension.class)
class AsciidocContentRenderingServletTest {

    SlingContext sc = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void doGet() throws Exception {
        // Given
        sc.build()
                .resource("/module/en_US/draft",
                        "jcr:primaryType", "pant:moduleVersion")
                .resource("/module/en_US/draft/content/asciidoc/jcr:content",
                        "jcr:data", "some asciidoc content")
                .commit();
        registerMockAdapter(Module.class, sc);
        AsciidocContentRenderingServlet servlet = new AsciidocContentRenderingServlet();
        Map<String, Object> params = Maps.newHashMap();
        params.put("draft", "true");
        sc.request().setParameterMap(params);
        sc.request().setResource(sc.resourceResolver().getResource("/module"));

        // When
        servlet.doGet(sc.request(), sc.response());

        // Then
        assertTrue(sc.response().getContentType().startsWith("text/plain"));
        assertEquals("some asciidoc content", sc.response().getOutputAsString());
    }
}