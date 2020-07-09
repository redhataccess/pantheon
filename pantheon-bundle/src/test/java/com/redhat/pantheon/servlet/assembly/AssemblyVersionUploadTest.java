package com.redhat.pantheon.servlet.assembly;

import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.assembly.Assembly;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.servlets.post.HtmlResponse;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class AssemblyVersionUploadTest {
    private SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void createFirstVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/repositories/test_workspace",
                        "jcr:primaryType", "pant:workspace")
                .commit();
        AssemblyVersionUpload upload = new AssemblyVersionUpload();
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
        assertNull(slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/assemblies/assembly1/es_ES/variants"));

        Assembly assembly =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/content/repositories/test_workspace/entities/assemblies/assembly1"),
                        Assembly.class);
        assertEquals("This is the adoc content",
                assembly.assemblyLocale(LocaleUtils.toLocale("es_ES")).get()
                        .getSource().get()
                        .draft().get()
                        .jcrContent().get()
                        .jcrData().get()
        );
    }
}
