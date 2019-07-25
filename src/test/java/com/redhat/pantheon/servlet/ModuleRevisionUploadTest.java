package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorPool;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.servlets.post.HtmlResponse;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class ModuleRevisionUploadTest {

    private SlingContext slingContext = new SlingContext();

    @Mock
    AsciidoctorPool asciidoctorPool;

    @Test
    void createFirstRevision() throws Exception {
        // Given
        lenient().when(asciidoctorPool.borrowObject()).thenReturn(mock(Asciidoctor.class, RETURNS_DEEP_STUBS));
        ModuleRevisionUpload upload = new ModuleRevisionUpload(asciidoctorPool);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "This is the adoc content");
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/module"));

        // when
        upload.doRun(slingContext.request(), new HtmlResponse(), null);

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/locales/es_ES/revisions/v1"));
    }

    @Test
    void createSecondRevision() {
        // Given
        slingContext.build()
                .resource("/new/module/locales/es_ES/revisions/v1/asciidoc/jcr:content",
                        "jcr:data", "This is the old adoc content")
                .commit();
        lenient().when(asciidoctorPool.borrowObject()).thenReturn(mock(Asciidoctor.class, RETURNS_DEEP_STUBS));
        ModuleRevisionUpload upload = new ModuleRevisionUpload(asciidoctorPool);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "This is the adoc content");
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/module"));

        // when
        upload.run(slingContext.request(), new HtmlResponse(), null);

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/new/module/locales/es_ES/revisions/v2"));
    }

    @Test
    @DisplayName("Avoid second revision if content is the same")
    void avoidSecondRevision() {
        // Given
        slingContext.build()
                .resource("/new/module/locales/es_ES/revisions/v1/asciidoc/jcr:content",
                        "jcr:data", "This is the old adoc content")
                .commit();
        ModuleRevisionUpload upload = new ModuleRevisionUpload(null);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "This is the old adoc content");
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/new/module"));

        // when
        upload.run(slingContext.request(), new HtmlResponse(), null);

        // Then
        assertNull(slingContext.resourceResolver().getResource("/new/module/locales/es_ES/revisions/v2"));
    }
}