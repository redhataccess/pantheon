package com.redhat.pantheon.servlet;

import com.redhat.pantheon.conf.AsciidoctorPoolService;
import com.redhat.pantheon.conf.LocalFileManagementService;
import com.redhat.pantheon.model.Module;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    @DisplayName("Generate html content from asciidoc")
    public void testGenerateHtmlFromAsciidoc() throws Exception {

        // Given
        String asciidocContent = "== This is a title \n\n And this is some text";
        slingContext.build()
                .resource("/module")
                .resource("cachedContent")
                .resource("/module/asciidoc/jcr:content",
                        "jcr:data", asciidocContent)
                .commit();
        slingContext.addModelsForClasses(Module.class, Module.CachedContent.class);
        Resource resource = slingContext.resourceResolver().getResource("/module");
        // needed mocks
        LocalFileManagementService lfmService = mock(LocalFileManagementService.class);
        AsciidoctorPoolService apService = mock(AsciidoctorPoolService.class);
        ServiceResourceResolverProvider serResResolverProvider = mock(ServiceResourceResolverProvider.class);
        // Test class
        AsciidocRenderingServlet servlet = new AsciidocRenderingServlet(lfmService, apService, serResResolverProvider);
        servlet.init();

        // When
        lenient().when(lfmService.getGemPaths()).thenReturn(getGemPaths());
        lenient().when(lfmService.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(apService.requestInstance(resource)).thenReturn(Asciidoctor.Factory.create(getGemPaths()));
        lenient().when(serResResolverProvider.getServiceResourceResolver()).thenReturn(slingContext.resourceResolver());
        slingContext.request().setResource(resource);

        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertTrue(slingContext.response().getOutputAsString().contains("This is a title"));
        assertTrue(slingContext.response().getOutputAsString().contains("And this is some text"));
        assertEquals("text/html", slingContext.response().getContentType());
    }

    @Test
    @DisplayName("Generate html content as cached")
    public void testRenderCachedHtmlContent() throws Exception {

        // Given
        slingContext.build()
                .resource("/module")
                .resource("cachedContent",
                        "jcr:data", "This is cached content",
                        "pant:hash", "01000000")
                .resource("/module/asciidoc/jcr:content",
                        "jcr:data", "")
                .commit();
        slingContext.addModelsForClasses(Module.class, Module.CachedContent.class);
        Resource resource = slingContext.resourceResolver().getResource("/module");
        // needed mocks
        LocalFileManagementService lfmService = mock(LocalFileManagementService.class);
        AsciidoctorPoolService apService = mock(AsciidoctorPoolService.class);
        ServiceResourceResolverProvider serResResolverProvider = mock(ServiceResourceResolverProvider.class);
        // Test class
        AsciidocRenderingServlet servlet = new AsciidocRenderingServlet(lfmService, apService, serResResolverProvider);
        servlet.init();

        // When
        lenient().when(lfmService.getGemPaths()).thenReturn(getGemPaths());
        lenient().when(lfmService.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(apService.requestInstance(resource)).thenReturn(Asciidoctor.Factory.create(getGemPaths()));
        lenient().when(serResResolverProvider.getServiceResourceResolver()).thenReturn(slingContext.resourceResolver());
        slingContext.request().setResource(resource);

        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        assertTrue(slingContext.response().getOutputAsString().contains("This is cached content"));
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
