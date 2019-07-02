package com.redhat.pantheon.asciidoctor;

import com.google.common.base.Function;
import com.redhat.pantheon.conf.AsciidoctorPoolService;
import com.redhat.pantheon.conf.LocalFileManagementService;
import com.redhat.pantheon.model.Module;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.asciidoctor.Asciidoctor;
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

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class AsciidoctorServiceTest {

    final SlingContext slingContext = new SlingContext();

    final Function<Resource, Module> mockSlingResourceAdapter = input -> new Module(input);

    @Mock
    LocalFileManagementService localFileManagementService;
    @Mock
    AsciidoctorPoolService asciidoctorPoolService;
    @Mock
    ServiceResourceResolverProvider serviceResourceResolverProvider;

    @Test
    public void testGetModuleHtml() throws IOException {
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
        // adapter (mock)
        slingContext.registerAdapter(Resource.class, Module.class, mockSlingResourceAdapter);

        // When
        lenient().when(localFileManagementService.getGemPaths()).thenReturn(getGemPaths());
        lenient().when(localFileManagementService.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(asciidoctorPoolService.requestInstance(resource)).thenReturn(Asciidoctor.Factory.create(getGemPaths()));
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());

        AsciidoctorService asciidoctorService =
                new AsciidoctorService(localFileManagementService, asciidoctorPoolService, serviceResourceResolverProvider);
        String generatedHtml = asciidoctorService.getModuleHtml(resource.adaptTo(Module.class), newHashMap(), false);

        // Then
        assertTrue(generatedHtml.contains("This is a title"));
        assertTrue(generatedHtml.contains("And this is some text"));
    }

    @Test
    public void testGetModuleHtmlWhenCached() throws Exception {

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
        // adapter (mock)
        slingContext.registerAdapter(Resource.class, Module.class, mockSlingResourceAdapter);

        // When
        lenient().when(localFileManagementService.getGemPaths()).thenReturn(getGemPaths());
        lenient().when(localFileManagementService.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(asciidoctorPoolService.requestInstance(resource))
                .thenReturn(Asciidoctor.Factory.create(getGemPaths()));
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());

        AsciidoctorService asciidoctorService =
                new AsciidoctorService(localFileManagementService, asciidoctorPoolService, serviceResourceResolverProvider);
        String generatedHtml = asciidoctorService.getModuleHtml(resource.adaptTo(Module.class), newHashMap(), false);

        // Then
        assertTrue(generatedHtml.contains("This is cached content"));
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