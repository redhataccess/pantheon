package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.extension.Events;
import com.redhat.pantheon.extension.url.CustomerPortalUrlUuidProvider;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.workspace.Workspace;
import com.redhat.pantheon.servlet.PublishDraftVersion;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.servlets.post.HtmlResponse;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.ModificationType;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class})
class PublishDraftVersionTest {

    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    //FIXME - temporary
    @Mock
    AsciidoctorService asciidoctorService;

    @Test
    void doRun() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/repositories/product/latest/1",
                        "jcr:primaryType","pant:productVersion",
                        "sling:resourceType","pantheon/productVersion")
                .commit();
        slingContext.build()
                .resource("/content/repositories/repo",
                        "jcr:primaryType", "pant:workspace",
                        "sling:resourceType", "pantheon/workspace")
                .resource("/content/repositories/repo/entities/module_variants/test",
                        "pant:canonical", true,
                        "pant:name", "test",
                        "pant:attributesFilePath","/tmp")
                .resource("/content/repositories/repo/entities")
                .resource("/content/repositories/repo/entities/module",
                        "jcr:primaryType", "pant:module")
                .resource("/content/repositories/repo/entities/module/en_US/variants",
                        "pant:canonical", true)
                .resource("/content/repositories/repo/entities/module/en_US/variants/DEFAULT/draft/metadata",
                        "jcr:title", "A draft title",
                        "productVersion", slingContext.currentResource("/content/repositories/product/latest/1").getValueMap().get("jcr:uuid"),
                        "urlFragment", "/test",
                        "pant:canonical", true)
                .resource("/content/repositories/repo/entities/module/en_US/source/draft/jcr:content",
                        "jcr:data", "The draft content")

                .commit();
        registerMockAdapter(Document.class, slingContext);
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(Workspace.class, slingContext);
        Events events = mock(Events.class);
        HtmlResponse postResponse = new HtmlResponse();
        List<Modification> changes = newArrayList();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/repo/entities/module") );
        slingContext.request().setParameterMap(Collections.singletonMap("variant", DocumentVariant.DEFAULT_VARIANT_NAME));

        ServiceResourceResolverProvider serviceResourceResolver = Mockito.mock(ServiceResourceResolverProvider.class);
        ResourceResolver resourceResolver = slingContext.request().getResourceResolver();
        Module module = slingContext.request().adaptTo(Module.class);
        lenient().doReturn(resourceResolver)
                .when(serviceResourceResolver).getServiceResourceResolver();
        //FIXME - asciidoctorService parameter is temporary
        PublishDraftVersion operation = new PublishDraftVersion(events, asciidoctorService, serviceResourceResolver);

        // When
        operation.doRun(slingContext.request(), postResponse, changes);

        // Then
        assertEquals(1, changes.size());
        assertEquals(ModificationType.MODIFY, changes.get(0).getType());
        assertEquals("/content/repositories/repo/entities/module", changes.get(0).getSource());
        assertEquals(HttpServletResponse.SC_OK, postResponse.getStatusCode());
        assertNotNull(slingContext.resourceResolver().getResource("/content/repositories/repo/entities/module/en_US/variants/DEFAULT/released"));
        assertNotNull(slingContext.resourceResolver().getResource("/content/repositories/repo/entities/module/en_US/variants/DEFAULT/released/metadata/pant:datePublished"));
        assertNotNull(slingContext.resourceResolver().getResource("/content/repositories/repo/entities/module/en_US/source/released/jcr:content"));
        assertNull(slingContext.resourceResolver().getResource("/content/repositories/repo/entities/module/en_US/source/draft/jcr:content"));
    }

    @Test
    @DisplayName("doRun for module with no draft version")
    void doRunNoDraftVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/repositories/repo/module",
                        "jcr:primaryType", "pant:module")
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/released/metadata",
                        "jcr:title", "A released title",
                        "productVersion", "123456",
                        "urlFragment", "/test")
                .resource("/content/repositories/repo/module/en_US/source/released/jcr:content",
                        "jcr:data", "The released content")
                .commit();
        registerMockAdapter(Document.class, slingContext);
        registerMockAdapter(Module.class, slingContext);
        HtmlResponse postResponse = new HtmlResponse();
        List<Modification> changes = newArrayList();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/repo/module") );
        ServiceResourceResolverProvider serviceResourceResolver = Mockito.mock(ServiceResourceResolverProvider.class);
        ResourceResolver resourceResolver = slingContext.request().getResourceResolver();
        Module module = slingContext.request().adaptTo(Module.class);
        lenient().doReturn(resourceResolver)
                .when(serviceResourceResolver).getServiceResourceResolver();
        //FIXME - asciidoctorService parameter is temporary
        PublishDraftVersion operation = new PublishDraftVersion(null, asciidoctorService, serviceResourceResolver);

        // When
        operation.doRun(slingContext.request(), postResponse, changes);

        // Then
        assertTrue(changes.size() == 0);
        assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, postResponse.getStatusCode());

    }
}