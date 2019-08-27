package com.redhat.pantheon.asciidoctor;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.module.Content;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleRevision;
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
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class AsciidoctorServiceTest {

    final SlingContext slingContext = new SlingContext();

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
                .resource("/module/locales/en_US/released/metadata")
                .resource("/module/locales/en_US/released/content")
                    .resource("asciidoc/jcr:content",
                            "jcr:data", asciidocContent)
                .commit();

        Resource moduleResource = slingContext.resourceResolver().getResource("/module");
        ModuleRevision moduleRevision =
                new ModuleRevision(slingContext.resourceResolver().getResource("/module/locales/en_US/released"));
        // adapter (mock)
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(Content.class, slingContext);
        lenient().when(globalConfig.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(asciidoctorPool.borrowObject(any()))
                .thenReturn(Asciidoctor.Factory.create());
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());
        AsciidoctorService asciidoctorService =
                new AsciidoctorService(globalConfig, asciidoctorPool, serviceResourceResolverProvider);

        // When
        String generatedHtml = asciidoctorService.getModuleHtml(moduleRevision, moduleResource, newHashMap(), false);

        // Then
        assertTrue(generatedHtml.contains("This is a title"));
        assertTrue(generatedHtml.contains("And this is some text"));
    }

    @Test
    public void testGetModuleHtmlWhenCached() throws Exception {

        // Given
        slingContext.build()
                .resource("/module/locales/en_US/released/metadata")
                .resource("/module/locales/en_US/released/content/asciidoc/jcr:content",
                                            "jcr:data", "")
                .resource("/module/locales/en_US/released/content/cachedHtml",
                                            "jcr:data", "This is cached content",
                                            "pant:hash", "01000000")
                .commit();
        Resource resource = slingContext.resourceResolver().getResource("/module");
        ModuleRevision moduleRevision =
                new ModuleRevision(slingContext.resourceResolver().getResource("/module/locales/en_US/released"));
        // adapter (mock)
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(Content.class, slingContext);

        // When
        lenient().when(globalConfig.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(asciidoctorPool.borrowObject(resource))
                .thenReturn(Asciidoctor.Factory.create());
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());

        AsciidoctorService asciidoctorService =
                new AsciidoctorService(globalConfig, asciidoctorPool, serviceResourceResolverProvider);
        String generatedHtml = asciidoctorService.getModuleHtml(moduleRevision, resource, newHashMap(), false);

        // Then
        assertTrue(generatedHtml.contains("This is cached content"));
    }

    @Test
    public void testExtractMetadata() throws Exception {

        // Given
        slingContext.build()
                .resource("/module/locales/en_US/released/metadata")
                .resource("/module/locales/en_US/released/content/asciidoc/jcr:content",
                        "jcr:data", "")
                .resource("/module/locales/en_US/released/content/cachedHtml",
                        "jcr:data", "This is cached content",
                        "pant:hash", "01000000")
                .commit();
        Resource resource = slingContext.resourceResolver().getResource("/module");
        ModuleRevision moduleRevision =
                new ModuleRevision(slingContext.resourceResolver().getResource("/module/locales/en_US/released"));
        // adapter (mock)
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(Content.class, slingContext);

        // When
        lenient().when(globalConfig.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(asciidoctorPool.borrowObject(resource))
                .thenReturn(Asciidoctor.Factory.create());
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());

        AsciidoctorService asciidoctorService =
                new AsciidoctorService(globalConfig, asciidoctorPool, serviceResourceResolverProvider);
        String generatedHtml = asciidoctorService.getModuleHtml(moduleRevision, resource, newHashMap(), false);

        // Then
        assertTrue(generatedHtml.contains("This is cached content"));
    }
}