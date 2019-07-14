package com.redhat.pantheon.asciidoctor;

import com.google.common.base.Function;
import com.redhat.pantheon.conf.GlobalConfig;
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
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class AsciidoctorServiceTest {

    final SlingContext slingContext = new SlingContext();

    final Function<Resource, Module> mockSlingResourceAdapter = input -> new Module(input);

    @Mock
    GlobalConfig globalConfig;
    @Mock
    AsciidoctorPool asciidoctorPool;
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
        lenient().when(globalConfig.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(asciidoctorPool.borrowObject(resource))
                .thenReturn(Asciidoctor.Factory.create());
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());

        AsciidoctorService asciidoctorService =
                new AsciidoctorService(globalConfig, asciidoctorPool, serviceResourceResolverProvider);
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
        lenient().when(globalConfig.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(asciidoctorPool.borrowObject(resource))
                .thenReturn(Asciidoctor.Factory.create());
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());

        AsciidoctorService asciidoctorService =
                new AsciidoctorService(globalConfig, asciidoctorPool, serviceResourceResolverProvider);
        String generatedHtml = asciidoctorService.getModuleHtml(resource.adaptTo(Module.class), newHashMap(), false);

        // Then
        assertTrue(generatedHtml.contains("This is cached content"));
    }
}