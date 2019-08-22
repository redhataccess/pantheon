package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorPool;
import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.model.module.Module;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.servlets.post.HtmlResponse;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class ModuleRevisionUploadTest {

    private SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Mock
    AsciidoctorPool asciidoctorPool;

    @Mock
    AsciidoctorService asciidoctorService;

    @Test
    void createFirstRevision() throws Exception {
        // Given
        lenient().when(asciidoctorPool.borrowObject()).thenReturn(mock(Asciidoctor.class, RETURNS_DEEP_STUBS));
        ModuleRevisionUpload upload = new ModuleRevisionUpload(asciidoctorPool, asciidoctorService);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "This is the adoc content");
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/module"));

        // when
        upload.doRun(slingContext.request(), new HtmlResponse(), null);

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/content"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/1/metadata"));
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/es_ES/draft"));
        assertNull(slingContext.resourceResolver().getResource("/new/module/es_ES/released"));

        Module module = new Module(slingContext.resourceResolver().getResource("/new/module"));
        assertEquals("This is the adoc content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent.get()
        );
    }

    @Test
    void createDraftRevisionOnTopOfReleased() throws Exception {
        // Given
        slingContext.build()
                // Released revision
                .resource("/new/module/es_ES/1",
                        "jcr:primaryType", "pant:moduleRevision")
                .resource("/new/module/es_ES/1/metadata")
                .resource("/new/module/es_ES/1/content/asciidoc/jcr:content",
                        "jcr:data", "This is the released adoc content")
                .commit();
        // set the draft and released 'pointers'
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("released", slingContext.resourceResolver().getResource("/new/module/es_ES/1").getValueMap().get("jcr:uuid"));

        lenient().when(asciidoctorPool.borrowObject()).thenReturn(mock(Asciidoctor.class, RETURNS_DEEP_STUBS));
        ModuleRevisionUpload upload = new ModuleRevisionUpload(asciidoctorPool, asciidoctorService);
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

        Module module = new Module(slingContext.resourceResolver().getResource("/new/module"));
        assertEquals("Draft asciidoc content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent.get()
        );
        assertEquals("This is the released adoc content",
                module.getReleasedContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent.get()
        );
    }

    @Test
    void modifyDraftRevision() throws Exception {
        // Given
        slingContext.build()
                .resource("/new/module/es_ES/1",
                        "jcr:primaryType", "pant:moduleRevision") // released
                .resource("/new/module/es_ES/2",
                        "jcr:primaryType", "pant:moduleRevision") // draft
                // Draft revision
                .resource("/new/module/es_ES/2/metadata")
                .resource("/new/module/es_ES/2/content/asciidoc/jcr:content",
                        "jcr:data", "This is the draft adoc content")
                // Released revision
                .resource("/new/module/es_ES/1/metadata")
                .resource("/new/module/es_ES/1/content/asciidoc/jcr:content",
                        "jcr:data", "This is the released adoc content")
                .commit();
        // set the draft and released 'pointers'
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("draft", slingContext.resourceResolver().getResource("/new/module/es_ES/2").getValueMap().get("jcr:uuid"));
        slingContext.resourceResolver().getResource("/new/module/es_ES").adaptTo(ModifiableValueMap.class)
                .put("released", slingContext.resourceResolver().getResource("/new/module/es_ES/1").getValueMap().get("jcr:uuid"));

        lenient().when(asciidoctorPool.borrowObject()).thenReturn(mock(Asciidoctor.class, RETURNS_DEEP_STUBS));
        ModuleRevisionUpload upload = new ModuleRevisionUpload(asciidoctorPool, asciidoctorService);
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

        Module module = new Module(slingContext.resourceResolver().getResource("/new/module"));
        assertEquals("Revised asciidoc content",
                module.getDraftContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent.get()
        );
        assertEquals("This is the released adoc content",
                module.getReleasedContent(LocaleUtils.toLocale("es_ES")).get().asciidocContent.get()
        );
    }
}