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

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        servlet.setDependencyProvider(new DependencyProvider() {

            @Override
            public List<String> getGemPaths() {
                System.out.println("Test getGemPaths");
                List<String> gems = new ArrayList<>();
//                try {
//                    Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources("META-INF");
//                    List<String> profiles = new ArrayList<>();
//                    if (en.hasMoreElements()) {
//                        URL url = en.nextElement();
//                        JarURLConnection urlcon = (JarURLConnection) (url.openConnection());
//                        try (JarFile jar = urlcon.getJarFile();) {
//                            Enumeration<JarEntry> entries = jar.entries();
//                            while (entries.hasMoreElements()) {
//                                String entry = entries.nextElement().getName();
//                                System.out.println(entry);
//                            }
//                        }
//                    }
//                } catch (Exception e) {}
                gems.addAll(Arrays.asList("uri:classloader:/gems/asciidoctor-1.5.8/lib"));
                gems.addAll(Arrays.asList("uri:classloader:/gems/coderay-1.1.0/lib"));
                gems.addAll(Arrays.asList("uri:classloader:/gems/concurrent-ruby-1.0.5-java/lib"));
                gems.addAll(Arrays.asList("uri:classloader:/gems/erubis-2.7.0/lib"));
                gems.addAll(Arrays.asList("uri:classloader:/gems/haml-4.0.5/lib"));
                gems.addAll(Arrays.asList("uri:classloader:/gems/open-uri-cached-0.0.5/lib"));
                gems.addAll(Arrays.asList("uri:classloader:/gems/slim-3.0.6/lib"));
                gems.addAll(Arrays.asList("uri:classloader:/gems/temple-0.7.7/lib"));
                gems.addAll(Arrays.asList("uri:classloader:/gems/thread_safe-0.3.6-java/lib"));
                gems.addAll(Arrays.asList("uri:classloader:/gems/tilt-2.0.8/lib"));
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
