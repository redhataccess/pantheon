package com.redhat.pantheon.servlet.module;

import com.google.common.collect.Maps;
import com.redhat.pantheon.model.module.Module;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SlingContextExtension.class)
class RawAsciidocServletTest {

    SlingContext sc = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void getRawDraftContent() throws Exception {
        // Given
        sc.build()
                .resource("/module",
                        "jcr:primaryType", "pant:module")
                .resource("/module/en_US/source/draft/jcr:content",
                        "jcr:data", "A source asciidoc string")
                .commit();
        registerMockAdapter(Module.class, sc);
        RawAsciidocServlet servlet = new RawAsciidocServlet();
        Map<String, Object> params = Maps.newHashMap();
        params.put("draft", "true");
        sc.request().setParameterMap(params);
        sc.request().setResource(sc.resourceResolver().getResource("/module"));

        // When
        servlet.doGet(sc.request(), sc.response());

        // Then
        assertTrue(sc.response().getContentType().startsWith("text/plain"));
        assertEquals("A source asciidoc string", sc.response().getOutputAsString());
    }

    @Test
    void nonExistentModule() throws Exception {
        // Given
        registerMockAdapter(Module.class, sc);
        RawAsciidocServlet servlet = new RawAsciidocServlet();
        Map<String, Object> params = Maps.newHashMap();
        params.put("draft", "true");
        sc.request().setParameterMap(params);
        sc.request().setResource(new NonExistingResource(sc.resourceResolver(), "/module"));

        // When
        servlet.doGet(sc.request(), sc.response());

        // Then
        assertEquals(HttpServletResponse.SC_NOT_FOUND, sc.response().getStatus());
    }
}