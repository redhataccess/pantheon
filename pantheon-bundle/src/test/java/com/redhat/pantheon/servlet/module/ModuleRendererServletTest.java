package com.redhat.pantheon.servlet.module;

import com.google.common.collect.ImmutableMap;
import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.module.Module;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class ModuleRendererServletTest {

    private final SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Mock AsciidoctorService asciidoctorService;

    @Test
    @DisplayName("Generate html content from asciidoc (released version, cached content, default variant)")
    public void testGenerateHtmlFromAsciidoc() throws Exception {
        // Given
        slingContext.build()
                .resource("/repo",
                        "jcr:primaryType", "pant:workspace",
                        "sling:resourceType", "pantheon/workspace"
                ).resource("/repo/entities/module",
                "jcr:primaryType", "pant:module")
                .resource("/repo/entities/module/en_US/source/released/jcr:content",
                        "jcr:data", "Some source content (irrelevant)")
                .resource("/repo/entities/module/en_US/variants/DEFAULT/released/metadata")
                .resource("/repo/entities/module/en_US/variants/DEFAULT/released/cached_html/jcr:content",
                        "jcr:data", "A generated html string")
                .commit();
        registerMockAdapter(Module.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module");
        slingContext.request().setResource(resource);
        slingContext.request().setParameterMap(
                ImmutableMap.<String, Object>builder()
                        .put(PantheonConstants.PARAM_VARIANT, "DEFAULT")
                        .build()
        );
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Module.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");

        // Test class
        ModuleRendererServlet moduleRendererServlet = new ModuleRendererServlet(asciidoctorService);
        moduleRendererServlet.init();

        // When
        moduleRendererServlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertEquals(HttpServletResponse.SC_OK, slingContext.response().getStatus());
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getDocumentHtml(
                any(Module.class),
                eq(Locale.US),
                eq("DEFAULT"),
                eq(false), // not draft
                anyMap(),
                eq(false)); // don't re-render
    }

    @Test
    @DisplayName("Generate html content from asciidoc (draft version, re-rendered)")
    public void testGenerateHtmlFromReleasedAsciidocWithRerender() throws Exception {

        // Given
        slingContext.build()
                .resource("/repo",
                        "jcr:primaryType", "pant:workspace",
                        "sling:resourceType", "pantheon/workspace"
                ).resource("/repo/entities/module",
                "jcr:primaryType", "pant:module")
                .resource("/repo/entities/module/en_US/source/draft/jcr:content",
                        "jcr:data", "Some source content (irrelevant)")
                .resource("/repo/entities/module/en_US/variants/DEFAULT/draft/metadata")
                .resource("/repo/entities/module/en_US/variants/DEFAULT/draft/cached_html/jcr:content",
                        "jcr:data", "A generated html string")
                .commit();
        registerMockAdapter(Module.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module");
        slingContext.request().setResource(resource);
        slingContext.request().setParameterMap(
                ImmutableMap.<String, Object>builder()
                        .put(PantheonConstants.PARAM_DRAFT, "true")
                        .put(PantheonConstants.PARAM_RERENDER, "true")
                        .put(PantheonConstants.PARAM_VARIANT, "DEFAULT")
                        .build()
        );
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Module.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");

        // Test class
        ModuleRendererServlet moduleRendererServlet = new ModuleRendererServlet(asciidoctorService);
        moduleRendererServlet.init();

        // When
        moduleRendererServlet.doGet(slingContext.request(), slingContext.response());
        // Then
        assertEquals(HttpServletResponse.SC_OK, slingContext.response().getStatus());
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getDocumentHtml(
                any(Module.class),
                eq(Locale.US),
                eq("DEFAULT"),
                eq(true), // draft
                anyMap(),
                eq(true)); // re-render
    }

    @Test
    @DisplayName("Generate html content from asciidoc (draft version, re-rendered, non-default variant)")
    public void testGenerateHtmlFromDraftAsciidocWithRerender() throws Exception {

        // Given
        slingContext.build()
                .resource("/repo",
                        "jcr:primaryType", "pant:workspace",
                        "sling:resourceType", "pantheon/workspace"
                ).resource("/repo/entities/module",
                "jcr:primaryType", "pant:module")
                .resource("/repo/entities/module/en_US/source/draft/jcr:content",
                        "jcr:data", "Some source content (irrelevant)")
                .resource("/repo/entities/module/en_US/variants/variant1/draft/metadata")
                .resource("/repo/entities/module/en_US/variants/variant1/draft/cached_html/jcr:content",
                        "jcr:data", "A generated html string")
                .commit();
        registerMockAdapter(Module.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module");
        slingContext.request().setResource(resource);
        slingContext.request().setParameterMap(
                ImmutableMap.<String, Object>builder()
                        .put(PantheonConstants.PARAM_DRAFT, "true")
                        .put(PantheonConstants.PARAM_RERENDER, "true")
                        .put(PantheonConstants.PARAM_VARIANT, "variant1")
                        .build()
        );
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Module.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");

        // Test class
        ModuleRendererServlet moduleRendererServlet = new ModuleRendererServlet(asciidoctorService);
        moduleRendererServlet.init();

        // When
        moduleRendererServlet.doGet(slingContext.request(), slingContext.response());


        // Then
        assertEquals(HttpServletResponse.SC_OK, slingContext.response().getStatus());
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getDocumentHtml(
                any(Module.class),
                eq(Locale.US),
                eq("variant1"), // non-default variant
                eq(true), // draft
                anyMap(),
                eq(true)); // re-render
    }

    @Test
    @DisplayName("Generate html content from asciidoc for an invalid variant")
    public void renderHtmlForInvalidVariant() throws Exception {

        // Given
        slingContext.build()
                .resource("/module",
                        "jcr:primaryType", "pant:module")
                .resource("/module/en_US/variants/DEFAULT/draft/cachedHtml/jcr:content",
                        "jcr:data", "A generated html string")
                .resource("/module/en_US/variants/DEFAULT/draft/metadata")
                .commit();
        registerMockAdapter(Module.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/module");
        slingContext.request().setResource(resource);
        slingContext.request().setParameterMap(
                ImmutableMap.<String, Object>builder()
                        .put(PantheonConstants.PARAM_DRAFT, "true")
                        .put(PantheonConstants.PARAM_RERENDER, "true")
                        .put(PantheonConstants.PARAM_VARIANT, "non_existing")
                        .build()
        );
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Module.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");

        // Test class
        ModuleRendererServlet moduleRendererServlet = new ModuleRendererServlet(asciidoctorService);
        moduleRendererServlet.init();

        // When
        moduleRendererServlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertEquals(HttpServletResponse.SC_NOT_FOUND, slingContext.response().getStatus());
    }

    @Test
    @DisplayName("Generate html content from asciidoc for an invalid locale")
    public void renderHtmlForInvalidLocale() throws Exception {

        // Given
        slingContext.build()
                .resource("/module",
                        "jcr:primaryType", "pant:module")
                .resource("/module/en_US/variants/DEFAULT/draft/cachedHtml/jcr:content",
                        "jcr:data", "A generated html string")
                .resource("/module/en_US/variants/DEFAULT/draft/metadata")
                .commit();
        registerMockAdapter(Module.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/module");
        slingContext.request().setResource(resource);
        slingContext.request().setParameterMap(
                ImmutableMap.<String, Object>builder()
                        .put(PantheonConstants.PARAM_DRAFT, "true")
                        .put(PantheonConstants.PARAM_RERENDER, "true")
                        .put(PantheonConstants.PARAM_LOCALE, new String[]{"ja_JP"})
                        .put(PantheonConstants.PARAM_VARIANT, "DEFAULT")
                        .build()
        );
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Module.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");

        // Test class
        ModuleRendererServlet moduleRendererServlet = new ModuleRendererServlet(asciidoctorService);
        moduleRendererServlet.init();

        // When
        moduleRendererServlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertEquals(HttpServletResponse.SC_NOT_FOUND, slingContext.response().getStatus());
    }
}
