package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.extension.Events;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVersion;
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
    @DisplayName("doRun for module with only released version")
    void doRun() throws Exception {
        // Given
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/released",
                        "jcr:primaryType", "pant:moduleVersion");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/released/metadata",
                        "jcr:title", "A published title");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/released/cached_html/jcr:content",
                        "jcr:data", "Released content");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/source/released/jcr:content",
                        "jcr:data", "Released content");
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(ModuleVersion.class, slingContext);
        Events events = mock(Events.class);
        HtmlResponse postResponse = new HtmlResponse();
        List<Modification> changes = newArrayList();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/repo/module") );
        UnpublishVersion operation = new UnpublishVersion(events);

        // When
        operation.doRun(slingContext.request(), postResponse, changes);

        // Then
        assertEquals(1, changes.size());
        assertEquals(ModificationType.MODIFY, changes.get(0).getType());
        assertEquals("/content/repositories/repo/module", changes.get(0).getSource());
        assertEquals(HttpServletResponse.SC_OK, postResponse.getStatusCode());
        assertNull(slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/variants/DEFAULT/released"));
        assertNotNull(slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/variants/DEFAULT/draft"));
        assertNull(slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/source/released"));

    }

    @Test
    @DisplayName("doRun for module with both released and draft version")
    void doRunWithDraftVersoin() throws Exception {
        // Given
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/released",
                        "jcr:primaryType", "pant:moduleVersion");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/released/metadata",
                        "jcr:title", "A published title");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/released/cached_html/jcr:content",
                        "jcr:data", "Released content");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/draft/cached_html/jcr:content",
                "jcr:data", "Draft content");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/source/draft/jcr:content",
                "jcr:data", "Draft content");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/source/released/jcr:content",
                        "jcr:data", "Released content");
        registerMockAdapter(Module.class, slingContext);
        registerMockAdapter(ModuleVersion.class, slingContext);
        Events events = mock(Events.class);
        HtmlResponse postResponse = new HtmlResponse();
        List<Modification> changes = newArrayList();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/repo/module") );
        UnpublishVersion operation = new UnpublishVersion(events);

        // When
        operation.doRun(slingContext.request(), postResponse, changes);

        // Then
        assertEquals(1, changes.size());
        assertEquals(ModificationType.MODIFY, changes.get(0).getType());
        assertEquals("/content/repositories/repo/module", changes.get(0).getSource());
        assertEquals(HttpServletResponse.SC_OK, postResponse.getStatusCode());
        assertNull(slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/variants/DEFAULT/released"));
        assertNotNull(slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/variants/DEFAULT/draft"));
        assertNull(slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/source/released"));
        assertNotNull(slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/source/draft"));

    }

    @Test
    @DisplayName("doRun for module with no released version")
    void doRunNoDraftVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/draft/metadata")
                .resource("/content/repositories/repo/module/en_US/variants/DEFAULT/draft/cached_html/jcr:content")
                .commit();
        registerMockAdapter(Module.class, slingContext);
        HtmlResponse postResponse = new HtmlResponse();
        List<Modification> changes = newArrayList();
        slingContext.request().setResource( slingContext.resourceResolver().getResource("/content/repositories/repo/module") );
        UnpublishVersion operation = new UnpublishVersion(null);

        // When
        operation.doRun(slingContext.request(), postResponse, changes);

        // Then
        assertTrue(changes.size() == 0);
        assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, postResponse.getStatusCode());
        assertNull(slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/variants/DEFAULT/released"));
        assertNull(slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/source/released"));

    }
}
