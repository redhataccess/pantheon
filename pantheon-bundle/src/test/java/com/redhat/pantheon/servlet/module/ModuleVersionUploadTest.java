package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.workspace.Workspace;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.servlets.post.HtmlResponse;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class ModuleVersionUploadTest {

    private SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Mock
    AsciidoctorService asciidoctorService;

    @Test
    void createFirstVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/repositories/test_workspace",
                        "jcr:primaryType", "pant:workspace")
                .resource("/content/repositories/test_workspace/module_variants/singleVariant",
                        "pant:canonical", true)
                .commit();

        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(Module.class), any(Locale.class), anyString(), anyBoolean(), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        registerMockAdapter(Workspace.class, slingContext);

        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "This is the adoc content");
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/content/repositories/test_workspace/entities/new/proc_module"));
        HtmlResponse response = new HtmlResponse();

        // when
        upload.doRun(slingContext.request(), response, null);

        // Then
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatusCode());
        assertNotNull(slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/new/proc_module/es_ES/source/draft/jcr:content"));
        assertNotNull(slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/new/proc_module/es_ES/variants"));

        Module module =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/new/proc_module"),
                        Module.class);
        assertEquals("This is the adoc content",
                module.locale(LocaleUtils.toLocale("es_ES")).get()
                        .source().get()
                        .draft().get()
                        .jcrContent().get()
                        .jcrData().get()
        );
        assertNotNull(
                module.locale(LocaleUtils.toLocale("es_ES")).get()
                        .source().get()
                        .draft().get()
                        .hash().get()
        );
        verify(asciidoctorService).getModuleHtml(any(Module.class), any(Locale.class), anyString(), eq(true), anyMap(), eq(true));
    }

    //@Disabled("temporarily unblock QA deployment")
    @Test
    void createFirstVersionUnicodeIso() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/repositories/test_workspace",
                        "jcr:primaryType", "pant:workspace")
                .commit();

        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(Module.class), any(Locale.class), anyString(), anyBoolean(), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        registerMockAdapter(Workspace.class, slingContext);

        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService);
        Map<String, Object> params = newHashMap();
        params.put("locale", Locale.SIMPLIFIED_CHINESE.toString());
        params.put("asciidoc", "å\u008D\u0097äº¬é\u0098²ç\u0096«ç\u008E°å\u009Cº");
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/content/repositories/test_workspace/entities/new/proc_module"));
        slingContext.request().setCharacterEncoding(StandardCharsets.ISO_8859_1.toString());
        HtmlResponse response = new HtmlResponse();

        // when
        upload.doRun(slingContext.request(), response, null);

        // Then
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatusCode());

        Module module =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/new/proc_module"),
                        Module.class);
        assertEquals("南京防疫现场",
                module
                        .locale(Locale.SIMPLIFIED_CHINESE).get()
                        .source().get()
                        .draft().get()
                        .jcrContent().get()
                        .jcrData().get()
        );
        assertNotNull(
                module.locale(Locale.SIMPLIFIED_CHINESE).get()
                        .source().get()
                        .draft().get()
                        .hash().get()
        );
        verify(asciidoctorService).getModuleHtml(any(Module.class), any(Locale.class), anyString(), eq(true), anyMap(), eq(true));
    }

    @Disabled("temporarily unblock QA deployment")
    @Test
    void createFirstVersionUnicodeUtf() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/repositories/test_workspace",
                        "jcr:primaryType", "pant:workspace")
                .commit();

        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(Module.class), any(Locale.class), anyString(), anyBoolean(), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        registerMockAdapter(Workspace.class, slingContext);

        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService);
        Map<String, Object> params = newHashMap();
        params.put("locale", Locale.SIMPLIFIED_CHINESE.toString());
        params.put("asciidoc", "南京防疫现场");
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/content/repositories/test_workspace/entities/new/proc_module"));
        slingContext.request().setCharacterEncoding(StandardCharsets.UTF_8.toString());
        HtmlResponse response = new HtmlResponse();

        // when
        upload.doRun(slingContext.request(), response, null);

        // Then
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatusCode());

        Module module =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/new/proc_module"),
                        Module.class);
        assertEquals("南京防疫现场",
                module
                        .locale(Locale.SIMPLIFIED_CHINESE).get()
                        .source().get()
                        .draft().get()
                        .jcrContent().get()
                        .jcrData().get()
        );
        assertNotNull(
                module.locale(Locale.SIMPLIFIED_CHINESE).get()
                        .source().get()
                        .draft().get()
                        .hash().get()
        );
        verify(asciidoctorService).getModuleHtml(any(Module.class), any(Locale.class), anyString(), eq(true), anyMap(), eq(true));
    }

    @Test
    void createDraftVersionOnTopOfReleased() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/repositories/test_workspace",
                        "jcr:primaryType", "pant:workspace")
                .resource("/content/repositories/test_workspace/entities/new/module",
                        "jcr:primaryType", "pant:moduleVersion")
                .resource("/content/repositories/test_workspace/entities/new/module/en_US/source/released/jcr:content",
                        "jcr:data", "This is the released adoc content")
                .commit();
        // set the draft and released 'pointers'

        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(Module.class), any(Locale.class), anyString(), anyBoolean(), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        registerMockAdapter(Workspace.class, slingContext);

        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService);
        Map<String, Object> params = newHashMap();
        params.put("locale", "en_US");
        params.put("asciidoc", "Draft asciidoc content");
        registerMockAdapter(Module.class, slingContext);
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/content/repositories/test_workspace/entities/new/module"));
        HtmlResponse response = new HtmlResponse();

        // when
        upload.doRun(slingContext.request(), response, null);

        // Then
        assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
        assertNotNull(slingContext.resourceResolver()
                .getResource("/content/repositories/test_workspace/entities/new/module/en_US/source/released"));
        assertNotNull(slingContext.resourceResolver()
                .getResource("/content/repositories/test_workspace/entities/new/module/en_US/source/draft"));

        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/new/module"), Module.class);
        assertEquals("Draft asciidoc content",
                module
                        .locale(Locale.US).get()
                        .source().get()
                        .draft().get()
                        .jcrContent().get()
                        .jcrData().get()
        );
        assertEquals("This is the released adoc content",
                module
                        .locale(Locale.US).get()
                        .source().get()
                        .released().get()
                        .jcrContent().get()
                        .jcrData().get()
        );
        verify(asciidoctorService).getModuleHtml(any(Module.class), any(Locale.class), anyString(), eq(true), anyMap(), eq(true));
    }

    @Test
    void modifyDraftVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/repositories/test_workspace",
                        "jcr:primaryType", "pant:workspace")
                .resource("/content/repositories/test_workspace/entities/new/module",
                        "jcr:primaryType", "pant:moduleVersion")
                .resource("/content/repositories/test_workspace/entities/new/module/en_US/source/draft/jcr:content",
                        "jcr:data", "This is the draft adoc content")
                .resource("/content/repositories/test_workspace/entities/new/module/en_US/source/released/jcr:content",
                        "jcr:data", "This is the released adoc content")
                .commit();

        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(Module.class), any(Locale.class), anyString(), anyBoolean(), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");
        registerMockAdapter(Workspace.class, slingContext);

        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService);
        Map<String, Object> params = newHashMap();
        params.put("locale", "en_US");
        params.put("asciidoc", "Revised asciidoc content");
        registerMockAdapter(Module.class, slingContext);
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/content/repositories/test_workspace/entities/new/module"));
        HtmlResponse response = new HtmlResponse();

        // when
        upload.doRun(slingContext.request(), response, null);

        // Then
        assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
        assertNotNull(slingContext.resourceResolver()
                .getResource("/content/repositories/test_workspace/entities/new/module/en_US/source/released"));
        assertNotNull(slingContext.resourceResolver()
                .getResource("/content/repositories/test_workspace/entities/new/module/en_US/source/draft"));

        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/new/module"), Module.class);
        assertEquals("Revised asciidoc content",
                module
                        .locale(Locale.US).get()
                        .source().get()
                        .draft().get()
                        .jcrContent().get()
                        .jcrData().get()
        );
        assertEquals("This is the released adoc content",
                module
                        .locale(Locale.US).get()
                        .source().get()
                        .released().get()
                        .jcrContent().get()
                        .jcrData().get()
        );
        verify(asciidoctorService).getModuleHtml(any(Module.class), any(Locale.class), anyString(), eq(true), anyMap(), eq(true));
    }

    @Test
    void uploadIdenticalDraftVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/repositories/test_workspace",
                        "jcr:primaryType", "pant:workspace")
                .resource("/content/repositories/test_workspace/entities/new/module",
                        "jcr:primaryType", "pant:moduleVersion")
                .resource("/content/repositories/test_workspace/entities/new/module/en_US/source/draft",
                        "jcr:primaryType", "nt:file",
                        "jcr:mixinTypes", new String[]{"pant:hashable"},
                        // this is the adler32 hash string for the source content being sent
                        "pant:hash", "f90a51a5")
                .resource("/content/repositories/test_workspace/entities/new/module/en_US/source/draft/jcr:content",
                        "jcr:primaryType", "nt:resource",
                        "jcr:data", "This is the draft adoc content")
                .resource("/content/repositories/test_workspace/entities/new/module/en_US/source/released/jcr:content",
                        "jcr:data", "This is the released adoc content")
                .commit();

        lenient().when(
                asciidoctorService.getModuleHtml(
                        any(Module.class), any(Locale.class), anyString(), anyBoolean(), anyMap(), anyBoolean()))
                .thenReturn("A generated html string");

        ModuleVersionUpload upload = new ModuleVersionUpload(asciidoctorService);
        Map<String, Object> params = newHashMap();
        params.put("locale", Locale.US);
        params.put("asciidoc", "This is the draft adoc content");
        registerMockAdapter(Module.class, slingContext);
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/content/repositories/test_workspace/entities/new/module"));
        HtmlResponse response = new HtmlResponse();

        // when
        upload.doRun(slingContext.request(), response, null);

        // Then
        assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatusCode());
        assertNotNull(slingContext.resourceResolver()
                .getResource("/content/repositories/test_workspace/entities/new/module/en_US/source/released"));
        assertNotNull(slingContext.resourceResolver()
                .getResource("/content/repositories/test_workspace/entities/new/module/en_US/source/draft"));

        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/new/module"), Module.class);
        assertEquals("This is the draft adoc content",
                module
                        .locale(Locale.US).get()
                        .source().get()
                        .draft().get()
                        .jcrContent().get()
                        .jcrData().get()
        );
        assertEquals("This is the released adoc content",
                module
                        .locale(Locale.US).get()
                        .source().get()
                        .released().get()
                        .jcrContent().get()
                        .jcrData().get()
        );
        verifyZeroInteractions(asciidoctorService);
    }
}
