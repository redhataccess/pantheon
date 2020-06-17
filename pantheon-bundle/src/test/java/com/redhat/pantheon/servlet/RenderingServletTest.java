package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.model.module.Module;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.Any;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class RenderingServletTest {

    private final SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Mock AsciidoctorService asciidoctorService;

    @Test
    @DisplayName("Generate html content from asciidoc (released version, cached content, default variant)")
    public void testGenerateHtmlFromModule() throws Exception {
        // Given
        slingContext.build()
                .resource("/repo",
                        "jcr:primaryType", "pant:workspace",
                        "sling:resourceType", "pantheon/workspace"
                ).resource("/repo/entities/module",
                "jcr:primaryType", "pant:workspace",
                "sling:resourceType", "pantheon/module")
                .resource("/repo/entities/module/en_US/source/released/jcr:content",
                        "jcr:data", "Some source content (irrelevant)")
                .resource("/repo/entities/module/en_US/variants/DEFAULT/released/metadata")
                .resource("/repo/entities/module/en_US/variants/DEFAULT/released/cached_html/jcr:content",
                        "jcr:data", "A generated html string")
                .commit();
            registerMockAdapter(Module.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module");
        slingContext.request().setResource(resource);
        AsciidoctorService asciidoctorService = mock(AsciidoctorService.class);
        ModuleRendering moduleRendering = mock(ModuleRendering.class);
//        when(moduleRendering.getRenderedHTML(slingContext.request(), slingContext.response())).then()
        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(Module.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");
        // Test class
        RenderingServlet servlet = new RenderingServlet(asciidoctorService);
        servlet.init();

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertEquals(HttpServletResponse.SC_OK, slingContext.response().getStatus());
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getModuleHtml(
                any(Module.class),
                eq(Locale.US),
                eq("DEFAULT"),
                eq(false), // not draft
                anyMap(),
                eq(false)); // don't re-render
    }
    @Test
    @DisplayName("Generate html content from asciidoc (released version, cached content, default variant)")
    public void testGenerateHtmlFromAssembly() throws Exception {
        // Given
        slingContext.build()
                .resource("/repo",
                        "jcr:primaryType", "pant:workspace",
                        "sling:resourceType", "pantheon/workspace"
                ).resource("/repo/entities/module",
                "jcr:primaryType", "pant:workspace",
                "sling:resourceType", "pantheon/assembly")
                .resource("/repo/entities/module/en_US/source/released/jcr:content",
                        "jcr:data", "Some source content (irrelevant)")
                .resource("/repo/entities/module/en_US/variants/DEFAULT/released/metadata")
                .resource("/repo/entities/module/en_US/variants/DEFAULT/released/cached_html/jcr:content",
                        "jcr:data", "A generated html string")
                .commit();
        registerMockAdapter(Module.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module");
        slingContext.request().setResource(resource);
        AsciidoctorService asciidoctorService = mock(AsciidoctorService.class);
        ModuleRendering moduleRendering = mock(ModuleRendering.class);
//        when(moduleRendering.getRenderedHTML(slingContext.request(), slingContext.response())).then()
        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(Module.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");
        // Test class
        RenderingServlet servlet = new RenderingServlet(asciidoctorService);
        servlet.init();

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertEquals(HttpServletResponse.SC_OK, slingContext.response().getStatus());
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getModuleHtml(
                any(Module.class),
                eq(Locale.US),
                eq("DEFAULT"),
                eq(false), // not draft
                anyMap(),
                eq(false)); // don't re-render
    }
}
