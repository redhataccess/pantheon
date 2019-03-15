package com.redhat.pantheon.servlet;

import com.redhat.pantheon.dependency.DependencyProvider;
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

import javax.jcr.Node;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        Resource contentResource = mock(Resource.class);
        Node contentNode = mock(Node.class);
        Node cacheNode = mock(Node.class);
        Resource fileNode = mock(Resource.class);
        ValueMap contentNodeVm = mock(ValueMap.class);
        String asciidocContent = "== This is a title \n\n And this is some text";
        AsciidocRenderingServlet servlet = new AsciidocRenderingServlet();
        servlet.setDependencyProvider(new DependencyProvider() {

            @Override
            public List<String> getGemPaths() throws IOException {
                List<String> gems = new ArrayList<>();
                Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources("gems");
                if (en.hasMoreElements()) {
                    URL url = en.nextElement();
                    JarURLConnection urlcon = (JarURLConnection) (url.openConnection());
                    try (JarFile jar = urlcon.getJarFile()) {
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            String entry = entries.nextElement().getName();
                            if (entry.startsWith("gems/") && entry.endsWith("/lib/")) {
                                gems.add("uri:classloader:/" + entry.substring(0, entry.lastIndexOf('/')));
                            }
                        }
                    }
                }
                return gems;
            }

            @Override
            public File getTemplateDir() {
                return null;
            }
        });
        servlet.init();

        // When
        lenient().when(resource.getPath()).thenReturn("/content");
        lenient().when(resource.adaptTo(Node.class)).thenReturn(contentNode);
        lenient().when(contentNode.getNode("cachedContent")).thenReturn(cacheNode);
        lenient().when(resource.getChild("asciidoc")).thenReturn(contentResource);
        lenient().when(contentResource.getChild("jcr:content")).thenReturn(fileNode);
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
