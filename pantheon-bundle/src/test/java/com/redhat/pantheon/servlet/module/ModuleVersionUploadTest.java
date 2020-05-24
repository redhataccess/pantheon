package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVersion;
import com.redhat.pantheon.model.module.ModuleType;
import com.redhat.pantheon.servlet.module.ModuleVersionUpload;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.post.HtmlResponse;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class ModuleVersionUploadTest {

    private SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Mock
    AsciidoctorService asciidoctorService;
    @Mock
    ServiceResourceResolverProvider serviceResourceResolverProvider;

    @Test
    void createFirstVersion() throws Exception {
        // Given
        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(ModuleVersion.class), any(Resource.class), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());
        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService, serviceResourceResolverProvider);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "This is the adoc content");
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/proc_module"));

        // when
        upload.doRun(slingContext.request(), new HtmlResponse(), null);

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/new/proc_module/es_ES/1/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/proc_module/es_ES/1/metadata"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/proc_module/es_ES/draft"));
        assertNull(slingContext.resourceResolver().getResource("/new/proc_module/es_ES/released"));

        Module module =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/new/proc_module"),
                        Module.class);
        assertEquals(ModuleType.PROCEDURE,
                module.getModuleLocale(LocaleUtils.toLocale("es_ES"))
                        .getVersion("1")
                        .metadata().get()
                        .moduleType().get());
        assertEquals("This is the adoc content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
        verify(asciidoctorService).getModuleHtml(
                any(ModuleVersion.class), any(Resource.class), anyMap(), eq(true));
    }

    @Test
    void createFirstVersionUnicodeIso() throws Exception {
        // Given
        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(ModuleVersion.class), any(Resource.class), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());
        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService, serviceResourceResolverProvider);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "å\u008D\u0097äº¬é\u0098²ç\u0096«ç\u008E°å\u009Cº");
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/proc_module"));
        slingContext.request().setCharacterEncoding(StandardCharsets.ISO_8859_1.toString());

        // when
        upload.doRun(slingContext.request(), new HtmlResponse(), null);

        // Then
        Module module =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/new/proc_module"),
                        Module.class);
        assertEquals("南京防疫现场",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
    }

    @Test
    void createFirstVersionUnicodeUtf() throws Exception {
        // Given
        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(ModuleVersion.class), any(Resource.class), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());
        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService, serviceResourceResolverProvider);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "南京防疫现场");
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/proc_module"));
        slingContext.request().setCharacterEncoding(StandardCharsets.UTF_8.toString());

        // when
        upload.doRun(slingContext.request(), new HtmlResponse(), null);

        // Then
        Module module =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/new/proc_module"),
                        Module.class);
        assertEquals("南京防疫现场",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
    }

    @Test
    void createDraftVersionOnTopOfReleased() throws Exception {
        // Given
        slingContext.build()
                // Released version
                .resource("/new/module/es_ES/1",
                        "jcr:primaryType", "pant:moduleVersion")
                .resource("/new/module/es_ES/1/metadata")
                .resource("/new/module/es_ES/1/content/asciidoc/jcr:content",
                        "jcr:data", "This is the released adoc content")
                .commit();
        // set the draft and released 'pointers'
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("released", slingContext.resourceResolver().getResource("/new/module/es_ES/1").getValueMap().get("jcr:uuid"));

        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(ModuleVersion.class), any(Resource.class), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());
        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService, serviceResourceResolverProvider);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "Draft asciidoc content");
        registerMockAdapter(Module.class, slingContext);
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/module"));

        // when
        upload.doRun(slingContext.request(), new HtmlResponse(), null);

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/metadata"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/2/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/2/metadata"));

        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/new/module"), Module.class);
        assertEquals("Draft asciidoc content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
        assertEquals("This is the released adoc content",
                module.getReleasedContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
        verify(asciidoctorService).getModuleHtml(
                any(ModuleVersion.class), any(Resource.class), anyMap(), eq(true));
    }

    @Test
    void modifyDraftVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/new/module/es_ES/1",
                        "jcr:primaryType", "pant:moduleVersion") // released
                .resource("/new/module/es_ES/2",
                        "jcr:primaryType", "pant:moduleVersion") // draft
                // Draft version
                .resource("/new/module/es_ES/2/metadata")
                .resource("/new/module/es_ES/2/content/asciidoc/jcr:content",
                        "jcr:data", "This is the draft adoc content")
                // Released version
                .resource("/new/module/es_ES/1/metadata")
                .resource("/new/module/es_ES/1/content/asciidoc/jcr:content",
                        "jcr:data", "This is the released adoc content")
                .commit();
        // set the draft and released 'pointers'
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("draft", slingContext.resourceResolver().getResource("/new/module/es_ES/2").getValueMap().get("jcr:uuid"));
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("released", slingContext.resourceResolver().getResource("/new/module/es_ES/1").getValueMap().get("jcr:uuid"));

        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(ModuleVersion.class), any(Resource.class), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());
        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService, serviceResourceResolverProvider);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "Revised asciidoc content");
        registerMockAdapter(Module.class, slingContext);
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/module"));

        // when
        upload.doRun(slingContext.request(), new HtmlResponse(), null);

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/2/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/2/metadata"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/metadata"));

        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/new/module"), Module.class);
        assertEquals("Revised asciidoc content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
        assertEquals("This is the released adoc content",
                module.getReleasedContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
        verify(asciidoctorService).getModuleHtml(
                any(ModuleVersion.class), any(Resource.class), anyMap(), eq(true));
    }

    @Test
    void uploadIdenticalDraftVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/new/module/es_ES/1",
                        "jcr:primaryType", "pant:moduleVersion") // released
                .resource("/new/module/es_ES/2",
                        "jcr:primaryType", "pant:moduleVersion") // draft
                // Draft version
                .resource("/new/module/es_ES/2/metadata")
                .resource("/new/module/es_ES/2/content/asciidoc/jcr:content",
                        "jcr:data", "This is the draft adoc content")
                .resource("/new/module/es_ES/2/content/cachedHtml",
                        "jcr:data", "This is the draft html content")
                // Released version
                .resource("/new/module/es_ES/1/metadata")
                .resource("/new/module/es_ES/1/content/asciidoc/jcr:content",
                        "jcr:data", "This is the released adoc content")
                .commit();
        // set the draft and released 'pointers'
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("draft", slingContext.resourceResolver().getResource("/new/module/es_ES/2").getValueMap().get("jcr:uuid"));
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("released", slingContext.resourceResolver().getResource("/new/module/es_ES/1").getValueMap().get("jcr:uuid"));

        slingContext.resourceResolver().getResource("/new/module/es_ES/1").adaptTo(ModifiableValueMap.class)
                .put("pant:hash", "bd7b5944327dce6ee8eb9573cb856d7528fbf8d634a4e8389a09f982571bf6c699f6dfefcd34fa6234e0acb19f46d1ee6d333a951476f1a712566bbc8d3552a2");
        slingContext.resourceResolver().getResource("/new/module/es_ES/2").adaptTo(ModifiableValueMap.class)
                .put("pant:hash", "bd7b5944327dce6ee8eb9573cb856d7528fbf8d634a4e8389a09f982571bf6c699f6dfefcd34fa6234e0acb19f46d1ee6d333a951476f1a712566bbc8d3552a2");

        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(ModuleVersion.class), any(Resource.class), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());
        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService, serviceResourceResolverProvider);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "This is the draft adoc content");
        registerMockAdapter(Module.class, slingContext);
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/module"));

        // when
        upload.doRun(slingContext.request(), new HtmlResponse(), null);

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/2/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/2/metadata"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/metadata"));

        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/new/module"), Module.class);
        assertEquals("This is the draft adoc content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
        assertEquals("This is the draft html content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().cachedHtml().get().data().get()
        );
        assertEquals("This is the released adoc content",
                module.getReleasedContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
        verify(asciidoctorService, never()).getModuleHtml(
                any(ModuleVersion.class), any(Resource.class), anyMap(), anyBoolean());
    }
    @Test
    void uploadIdenticalDraftVersionWithDifferentHash() throws Exception {
        // Given
        slingContext.build()
                .resource("/new/module/es_ES/1",
                        "jcr:primaryType", "pant:moduleVersion") // released
                .resource("/new/module/es_ES/2",
                        "jcr:primaryType", "pant:moduleVersion") // draft
                // Draft version
                .resource("/new/module/es_ES/2/metadata")
                .resource("/new/module/es_ES/2/content/asciidoc/jcr:content",
                        "jcr:data", "This is the draft adoc content")
                .resource("/new/module/es_ES/2/content/cachedHtml",
                        "jcr:data", "This is the draft html content")
                // Released version
                .resource("/new/module/es_ES/1/metadata")
                .resource("/new/module/es_ES/1/content/asciidoc/jcr:content",
                        "jcr:data", "This is the released adoc content")
                .commit();
        // set the draft and released 'pointers'
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("draft", slingContext.resourceResolver().getResource("/new/module/es_ES/2").getValueMap().get("jcr:uuid"));
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("released", slingContext.resourceResolver().getResource("/new/module/es_ES/1").getValueMap().get("jcr:uuid"));

        slingContext.resourceResolver().getResource("/new/module/es_ES/1").adaptTo(ModifiableValueMap.class)
                .put("pant:hash", "bd7b5944327dce6ee8eb9573cb856d7528fbf8d634a4e8389a09f982571bf6c699f6dfefcd34fa6234e0acb19f46d1ee6d333a951476f1a712566bbc8d3552a2");
        slingContext.resourceResolver().getResource("/new/module/es_ES/2").adaptTo(ModifiableValueMap.class)
                .put("pant:hash", "cd7b5944327dce6ee8eb9573cb856d7528fbf8d634a4e8389a09f982571bf6c699f6dfefcd34fa6234e0acb19f46d1ee6d333a951476f1a712566bbc8d3552a2");

        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(ModuleVersion.class), any(Resource.class), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());
        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService, serviceResourceResolverProvider);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "This is the draft adoc content");
        registerMockAdapter(Module.class, slingContext);
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/module"));

        // when
        upload.doRun(slingContext.request(), new HtmlResponse(), null);

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/2/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/2/metadata"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/metadata"));

        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/new/module"), Module.class);
        assertEquals("This is the draft adoc content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
        assertEquals("This is the draft html content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().cachedHtml().get().data().get()
        );
        assertEquals("This is the released adoc content",
                module.getReleasedContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
        verify(asciidoctorService, never()).getModuleHtml(
                any(ModuleVersion.class), any(Resource.class), anyMap(), anyBoolean());
    }

    @Test
    void uploadIdenticalDraftVersionWithNoHash() throws Exception {
        // Given
        slingContext.build()
                .resource("/new/module/es_ES/1",
                        "jcr:primaryType", "pant:moduleVersion") // released
                .resource("/new/module/es_ES/2",
                        "jcr:primaryType", "pant:moduleVersion") // draft
                // Draft version
                .resource("/new/module/es_ES/2/metadata")
                .resource("/new/module/es_ES/2/content/asciidoc/jcr:content",
                        "jcr:data", "This is the draft adoc content")
                .resource("/new/module/es_ES/2/content/cachedHtml",
                        "jcr:data", "This is the draft html content")
                // Released version
                .resource("/new/module/es_ES/1/metadata")
                .resource("/new/module/es_ES/1/content/asciidoc/jcr:content",
                        "jcr:data", "This is the released adoc content")
                .commit();
        // set the draft and released 'pointers'
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("draft", slingContext.resourceResolver().getResource("/new/module/es_ES/2").getValueMap().get("jcr:uuid"));
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("released", slingContext.resourceResolver().getResource("/new/module/es_ES/1").getValueMap().get("jcr:uuid"));

        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(ModuleVersion.class), any(Resource.class), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        lenient().when(serviceResourceResolverProvider.getServiceResourceResolver())
                .thenReturn(slingContext.resourceResolver());
        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService, serviceResourceResolverProvider);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "This is the draft adoc content");
        registerMockAdapter(Module.class, slingContext);
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/module"));

        // when
        upload.doRun(slingContext.request(), new HtmlResponse(), null);

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/2/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/2/metadata"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/metadata"));

        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/new/module"), Module.class);
        assertEquals("This is the draft adoc content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
        assertEquals("This is the draft html content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().cachedHtml().get().data().get()
        );
        assertEquals("This is the released adoc content",
                module.getReleasedContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent().get()
        );
        verify(asciidoctorService, never()).getModuleHtml(
                any(ModuleVersion.class), any(Resource.class), anyMap(), anyBoolean());
    }
}