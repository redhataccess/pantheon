package com.redhat.pantheon.asciidoctor;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.Content;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVersion;
import com.redhat.pantheon.model.workspace.Workspace;
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
import java.util.Locale;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
                .resource("/repoParent",
                        "jcr:primaryType", "pant:workspace",
                        "sling:resourceType", "pantheon/workspace"
                        )
                .resource("/repoParent/entities/module/en_US/source/released/jcr:content",
                    "jcr:data", asciidocContent)
                .resource("/repoParent/entities/module/en_US/variants/test/released/metadata",
                        "jcr:title", "A draft title", "jcr:primaryType", "nt:unstructured", "pant:dateUploaded", "2020-02-12 19:20:01")
                .resource("/repoParent/entities/module/en_US/variants/test/released/cached_html/jcr:content",
                            "jcr:data", asciidocContent)
                .commit();

        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/repoParent/entities/module"),
                        Module.class);
        // adapter (mock)
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(Content.class, slingContext);
        registerMockAdapter(ModuleVersion.class, slingContext);
        registerMockAdapter(Workspace.class, slingContext);
        lenient().when(globalConfig.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(asciidoctorPool.borrowObject())
                .thenReturn(Asciidoctor.Factory.create());
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());
        AsciidoctorService asciidoctorService =
                new AsciidoctorService(globalConfig, asciidoctorPool, serviceResourceResolverProvider);

        // When
        String generatedHtml = asciidoctorService.getModuleHtml(module, new Locale("en", "US"), "test", false, newHashMap(), false);

        // Then
        assertTrue(generatedHtml.contains("This is a title"));
        assertTrue(generatedHtml.contains("And this is some text"));
    }

    @Test
    public void testGetModuleHtmlWhenCached() throws Exception {

        // Given
        slingContext.build()
                .resource("/repoParent",
                        "jcr:primaryType", "pant:workspace",
                        "sling:resourceType", "pantheon/workspace"
                )
                .resource("/repoParent/entities/module/en_US/source/released/jcr:content",
                    "jcr:data", "This is my content")
                .resource("/repoParent/entities/module/en_US/variants/test/released/metadata")
                .resource("/repoParent/entities/module/en_US/variants/test/released/cached_html/jcr:content",
                                            "jcr:data", "This is cached content",
                                            "pant:hash", "01000000")
                .commit();
        Resource resource = slingContext.resourceResolver().getResource("/repoParent/module");
        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/repoParent/entities/module"),
                        Module.class);

        // adapter (mock)
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(ModuleVersion.class, slingContext);

        // When
        lenient().when(globalConfig.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(asciidoctorPool.borrowObject())
                .thenReturn(Asciidoctor.Factory.create());
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());

        AsciidoctorService asciidoctorService =
                new AsciidoctorService(globalConfig, asciidoctorPool, serviceResourceResolverProvider);
        String generatedHtml = asciidoctorService.getModuleHtml(module, new Locale("en", "US"), "test", false, newHashMap(), false);

        // Then
        assertTrue(generatedHtml.contains("This is cached content"));
    }

    @Test
    public void testGetModuleHtmlWithAttributeFileNotFound() throws IOException {
        // Given
        String asciidocContent = "== This is {product}";
        slingContext.build()
                .resource("/content/repositories/linux",
                        "jcr:primaryType", "pant:workspace",
                        // see WorkspaceChild#getWorkspace
                        "sling:resourceType", "pantheon/workspace")
                .resource("/content/repositories/linux/module_variants/fedora",
                        "pant:attributesFilePath", "/my/atts.adoc",
                        "pant:canonical", true)
                .resource("/content/repositories/linux/entities/module",
                        "jcr:primaryType", "pant:module")
                .resource("/content/repositories/linux/entities/module/en_US/source/draft/jcr:content",
                         "jcr:data", asciidocContent)
                .resource("/content/repositories/linux/entities/module/en_US/variants/fedora/draft/metadata",
                        "jcr:title", "A draft title",
                        "jcr:primaryType", "nt:unstructured",
                        "pant:dateUploaded", "2020-02-12 19:20:01")
                .commit();

        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/repositories/linux/entities/module"),
                        Module.class);

        // adapter (mock)
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(Content.class, slingContext);
        registerMockAdapter(ModuleVersion.class, slingContext);
        registerMockAdapter(Workspace.class, slingContext);
        lenient().when(globalConfig.getTemplateDirectory()).thenReturn(Optional.empty());
        lenient().when(asciidoctorPool.borrowObject())
                .thenReturn(Asciidoctor.Factory.create());
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());
        AsciidoctorService asciidoctorService =
                new AsciidoctorService(globalConfig, asciidoctorPool, serviceResourceResolverProvider);

        // When
        String generatedHtml = asciidoctorService.getModuleHtml(module, new Locale("en", "US"), "fedora", true, newHashMap(), false);

        // Then
        assertTrue(generatedHtml.contains("Invalid include: /content/repositories/linux/entities/my/atts.adoc"));
    }
}