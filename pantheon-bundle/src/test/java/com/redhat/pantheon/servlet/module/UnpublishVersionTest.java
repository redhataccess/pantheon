package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.extension.Events;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVersion;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.servlets.post.HtmlResponse;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.ModificationType;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith({SlingContextExtension.class})
class UnpublishVersionTest {

    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void doRun() throws Exception {
        // Given
        slingContext.create()
                .resource("/module/en_US/1",
                        "jcr:primaryType", "pant:moduleVersion");
        slingContext.create()
                .resource("/module/en_US/1/metadata",
                        "jcr:title", "A published title");
        slingContext.create()
                .resource("/module/en_US/1/content/asciidoc/jcr:content",
                        "jcr:data", "Released content");
        slingContext.resourceResolver().getResource("/module/en_US").adaptTo(ModifiableValueMap.class)
                .put("released", slingContext.resourceResolver().getResource("/module/en_US/1").getValueMap().get("jcr:uuid"));
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(ModuleVersion.class, slingContext);
        Events events = mock(Events.class);
        HtmlResponse postResponse = new HtmlResponse();
        List<Modification> changes = newArrayList();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/module") );
        UnpublishVersion operation = new UnpublishVersion(events);

        // When
        operation.doRun(slingContext.request(), postResponse, changes);

        // Then
        assertEquals(1, changes.size());
        assertEquals(ModificationType.MODIFY, changes.get(0).getType());
        assertEquals("/module", changes.get(0).getSource());
        assertEquals(HttpServletResponse.SC_OK, postResponse.getStatusCode());
        assertNull(slingContext.resourceResolver().getResource("/module/en_US/released"));
        assertNotNull(slingContext.resourceResolver().getResource("/module/en_US/draft"));

    }

    @Test
    @DisplayName("doRun for module with no released version")
    void doRunNoDraftVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/module/locales/en_US/released/metadata")
                .resource("/module/locales/en_US/released/content/asciidoc/jcr:content")
                .commit();
        registerMockAdapter(Module.class, slingContext);
        HtmlResponse postResponse = new HtmlResponse();
        List<Modification> changes = newArrayList();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/module") );
        UnpublishVersion operation = new UnpublishVersion(null);

        // When
        operation.doRun(slingContext.request(), postResponse, changes);

        // Then
        assertTrue(changes.size() == 0);
        assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, postResponse.getStatusCode());
    }
}
