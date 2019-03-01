package com.redhat.pantheon.servlet;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.MockSling;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class AsciidocRenderingServletTest {

    private final SlingContext slingContext = new SlingContext();

    private ResourceResolver resourceResolver = MockSling.newResourceResolver(slingContext.bundleContext());

    @Mock
    private Resource resource;

    @Test
    @DisplayName("Generate html content from asciidoc")
    public void testGenerateHtmlFromAsciidoc() throws Exception {

        // Given
        Resource contentNode = mock(Resource.class);
        Resource fileNode = mock(Resource.class);
        Resource cachedContentNode = mock(Resource.class);
        ValueMap contentNodeVm = mock(ValueMap.class);
        String asciidocContent = "== This is a title \n\n And this is some text";
        AsciidocRenderingServlet servlet = new AsciidocRenderingServlet();
        servlet.init();

        // When
        lenient().when(resource.getPath()).thenReturn("/content");
        lenient().when(resource.getChild("asciidoc")).thenReturn(contentNode);
        lenient().when(contentNode.getChild("jcr:content")).thenReturn(fileNode);
        lenient().when(resource.getChild("cachedContent")).thenReturn(null);
        when(fileNode.getValueMap()).thenReturn(contentNodeVm);
        when(contentNodeVm.get( eq("jcr:data"), eq(String.class) )).thenReturn( asciidocContent );
        slingContext.request().setResource(resource);

        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertTrue(slingContext.response().getOutputAsString().contains("This is a title"));
        assertTrue(slingContext.response().getOutputAsString().contains("And this is some text"));
        assertEquals("text/html", slingContext.response().getContentType());
    }
}
