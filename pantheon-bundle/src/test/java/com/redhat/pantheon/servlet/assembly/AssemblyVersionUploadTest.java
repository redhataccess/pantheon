package com.redhat.pantheon.servlet.assembly;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.assembly.Assembly;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.workspace.Workspace;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.servlets.post.HtmlResponse;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class AssemblyVersionUploadTest {
    private SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Mock
    AsciidoctorService asciidoctorService;

    @Test
    void createFirstVersion() throws Exception {
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

        AssemblyVersionUpload upload = new AssemblyVersionUpload(asciidoctorService);
        Map<String, Object> params = newHashMap();
        params.put("locale", "es_ES");
        params.put("asciidoc", "This is the adoc content");
        slingContext.request().setParameterMap(params);
        slingContext.request().setResource(new NonExistingResource(slingContext.resourceResolver(), "/content/repositories/test_workspace/entities/assemblies/assembly1"));
        HtmlResponse response = new HtmlResponse();

        // when
        upload.doRun(slingContext.request(), response, null);

        // Then
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatusCode());
        assertNotNull(slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/assemblies/assembly1/es_ES/source/draft/jcr:content"));
        assertNotNull(slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/assemblies/assembly1/es_ES/variants"));

        Assembly assembly =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/assemblies/assembly1"),
                        Assembly.class);
        assertEquals("This is the adoc content",
                assembly.locale(LocaleUtils.toLocale("es_ES")).get()
                        .source().get()
                        .draft().get()
                        .jcrContent().get()
                        .jcrData().get()
        );
        assertNotNull(
                assembly.locale(LocaleUtils.toLocale("es_ES")).get()
                        .source().get()
                        .draft().get()
                        .hash().get()
        );
        verify(asciidoctorService).getModuleHtml(any(Assembly.class), any(Locale.class), anyString(), eq(true), anyMap(), eq(true));
    }
}
