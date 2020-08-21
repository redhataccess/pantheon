package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.servlet.module.ModuleRendererServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class DocumentVariantRenderServletTest {

    private final SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Mock
    AsciidoctorService asciidoctorService;

    @Test
    public void testRequestReleasedReleasedOnly() throws Exception {
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
                .commit();
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(DocumentVariant.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module/en_US/variants/DEFAULT");
        slingContext.request().setResource(resource);
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Document.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");
        DocumentVariantRenderServlet servlet = new DocumentVariantRenderServlet(asciidoctorService);

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertEquals(HttpServletResponse.SC_OK, slingContext.response().getStatus());
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getDocumentHtml(
                any(Document.class),
                eq(Locale.US),
                eq("DEFAULT"),
                eq(false), // not draft
                anyMap(),
                eq(false)); // don't re-render
    }

    @Test
    public void testRequestReleasedDraftOnly() throws Exception {
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
                .commit();
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(DocumentVariant.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module/en_US/variants/DEFAULT");
        slingContext.request().setResource(resource);
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Document.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");

        // Purposefully not explicitly calling /released, to make sure that released is the implicit version.
        // If that ever becomes untrue, this test should fail.
        DocumentVariantRenderServlet servlet = new DocumentVariantRenderServlet(asciidoctorService);

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertEquals(HttpServletResponse.SC_NOT_FOUND, slingContext.response().getStatus());
        assertTrue(slingContext.response().getStatusMessage().contains("Released content not found"));
    }

    @Test
    public void testRequestReleasedBoth() throws Exception {
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
                .resource("/repo/entities/module/en_US/source/draft/jcr:content",
                        "jcr:data", "Some more source content (irrelevant)")
                .resource("/repo/entities/module/en_US/variants/DEFAULT/draft/metadata")
                .commit();
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(DocumentVariant.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module/en_US/variants/DEFAULT");
        slingContext.request().setResource(resource);
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Document.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");
        DocumentVariantRenderServlet servlet = new DocumentVariantRenderServlet(asciidoctorService) {

            @Override
            protected String getSuffix(SlingHttpServletRequest request) {
                return "/released"; //Don't need to be explicit here, just including it in one of the tests for coverage
            }
        };

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertEquals(HttpServletResponse.SC_OK, slingContext.response().getStatus());
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getDocumentHtml(
                any(Document.class),
                eq(Locale.US),
                eq("DEFAULT"),
                eq(false), // not draft
                anyMap(),
                eq(false)); // don't re-render
    }

    @Test
    public void testRequestLatestDraftOnly() throws Exception {
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
                .commit();
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(DocumentVariant.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module/en_US/variants/DEFAULT");
        slingContext.request().setResource(resource);
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Document.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");
        DocumentVariantRenderServlet servlet = new DocumentVariantRenderServlet(asciidoctorService) {

            @Override
            protected String getSuffix(SlingHttpServletRequest request) {
                return "/latest";
            }
        };

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertEquals(HttpServletResponse.SC_OK, slingContext.response().getStatus());
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getDocumentHtml(
                any(Document.class),
                eq(Locale.US),
                eq("DEFAULT"),
                eq(true), // draft
                anyMap(),
                eq(false)); // don't re-render
    }

    @Test
    public void testRequestLatestReleasedOnly() throws Exception {
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
                .commit();
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(DocumentVariant.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module/en_US/variants/DEFAULT");
        slingContext.request().setResource(resource);
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Document.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");
        DocumentVariantRenderServlet servlet = new DocumentVariantRenderServlet(asciidoctorService) {

            @Override
            protected String getSuffix(SlingHttpServletRequest request) {
                return "/latest";
            }
        };

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertEquals(HttpServletResponse.SC_OK, slingContext.response().getStatus());
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getDocumentHtml(
                any(Document.class),
                eq(Locale.US),
                eq("DEFAULT"),
                eq(false), // released
                anyMap(),
                eq(false)); // don't re-render
    }

    @Test
    public void testRequestLatestBoth() throws Exception {
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
                .resource("/repo/entities/module/en_US/source/draft/jcr:content",
                        "jcr:data", "Some more source content (irrelevant)")
                .resource("/repo/entities/module/en_US/variants/DEFAULT/draft/metadata")
                .commit();
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(DocumentVariant.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module/en_US/variants/DEFAULT");
        slingContext.request().setResource(resource);
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Document.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");
        DocumentVariantRenderServlet servlet = new DocumentVariantRenderServlet(asciidoctorService) {

            @Override
            protected String getSuffix(SlingHttpServletRequest request) {
                return "/latest";
            }
        };

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertEquals(HttpServletResponse.SC_OK, slingContext.response().getStatus());
        assertTrue(slingContext.response().getOutputAsString().contains("A generated html string"));
        assertEquals("text/html", slingContext.response().getContentType());
        verify(asciidoctorService).getDocumentHtml(
                any(Document.class),
                eq(Locale.US),
                eq("DEFAULT"),
                eq(true), // not draft
                anyMap(),
                eq(false)); // don't re-render
    }

    @Test
    public void testBadSuffix() throws Exception {
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
                .resource("/repo/entities/module/en_US/source/draft/jcr:content",
                        "jcr:data", "Some more source content (irrelevant)")
                .resource("/repo/entities/module/en_US/variants/DEFAULT/draft/metadata")
                .commit();
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(DocumentVariant.class, slingContext);
        Resource resource = slingContext.resourceResolver().getResource("/repo/entities/module/en_US/variants/DEFAULT");
        slingContext.request().setResource(resource);
        lenient().when(
                asciidoctorService.getDocumentHtml(
                        any(Document.class),
                        any(Locale.class),
                        anyString(),
                        anyBoolean(),
                        anyMap(),
                        anyBoolean()))
                .thenReturn("A generated html string");
        DocumentVariantRenderServlet servlet = new DocumentVariantRenderServlet(asciidoctorService) {

            @Override
            protected String getSuffix(SlingHttpServletRequest request) {
                return "/banana";
            }
        };

        // When/Then
        assertThrows(ServletException.class,
                () -> servlet.doGet(slingContext.request(), slingContext.response()),
                "Unrecognized suffix: /banana. Valid values are '/latest', '/released', and unspecified.");
    }
}
