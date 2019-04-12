package com.redhat.pantheon.servlet;

import com.redhat.pantheon.conf.AsciidoctorPoolService;
import com.redhat.pantheon.conf.LocalFileManagementService;
import com.redhat.pantheon.model.Module;
import com.redhat.pantheon.model.Module.CachedContent;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.MockSling;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

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
        Module module = mock(Module.class);
        CachedContent cachedContent = mock(CachedContent.class);
        LocalFileManagementService lfmService = mock(LocalFileManagementService.class);
        AsciidoctorPoolService apService = mock(AsciidoctorPoolService.class);
        String asciidocContent = "== This is a title \n\n And this is some text";
        AsciidocRenderingServlet servlet = new AsciidocRenderingServlet(lfmService, apService);
        servlet.init();

        // When
        lenient().when(resource.getPath()).thenReturn("/content");
        lenient().when(resource.adaptTo(Module.class)).thenReturn(module);
        lenient().when(module.getCachedContent()).thenReturn(cachedContent);
        lenient().when(module.getAsciidocContent()).thenReturn(asciidocContent);
        lenient().when(lfmService.getGemPaths()).thenReturn(getGemPaths());
        lenient().when(lfmService.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(apService.requestInstance(resource)).thenReturn(Asciidoctor.Factory.create(getGemPaths()));
        slingContext.request().setResource(resource);

        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertTrue(slingContext.response().getOutputAsString().contains("This is a title"));
        assertTrue(slingContext.response().getOutputAsString().contains("And this is some text"));
        assertEquals("text/html", slingContext.response().getContentType());
    }

    private List<String> getGemPaths() throws IOException {
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
}
