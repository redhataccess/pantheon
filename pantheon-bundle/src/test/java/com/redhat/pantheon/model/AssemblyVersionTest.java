package com.redhat.pantheon.model;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.assembly.Assembly;
import com.redhat.pantheon.model.assembly.AssemblyPage;
import com.redhat.pantheon.model.assembly.TableOfContents;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.model.workspace.Workspace;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import java.util.Locale;

import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class AssemblyVersionTest {

    private final SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void consumeTableOfContents() throws Exception {
        // Given
        slingContext.build()
                .resource("/content",
                        "sling:resourceType", "pantheon/workspace"
                )
                .resource("/content/assembly1")
                .commit();
        slingContext.create().resource("/content/module1",
                "jcr:mixinTypes", "mix:referenceable");
        slingContext.create().resource("/content/module2",
                "jcr:mixinTypes", "mix:referenceable");
        slingContext.registerAdapter(Resource.class, Node.class, mock(Node.class));
        registerMockAdapter(Workspace.class, slingContext);
        registerMockAdapter(AssemblyPage.class, slingContext);
        Assembly assembly =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/assembly1"),
                        Assembly.class);
        Module m1 = SlingModels.getModel(slingContext.resourceResolver().getResource("/content/module1"), Module.class);
        Module m2 = SlingModels.getModel(slingContext.resourceResolver().getResource("/content/module2"), Module.class);
        String m1uuid = m1.uuid().get();
        String m2uuid = m2.uuid().get();

        TableOfContents toc = new TableOfContents();
        toc.addEntry(1, m1);
        toc.addEntry(0, m2);

        // When
        assembly.locale(GlobalConfig.DEFAULT_MODULE_LOCALE).create()
                .variants().getOrCreate()
                .canonicalVariant().getOrCreate()
                .draft().getOrCreate()
                .consumeTableOfContents(toc);
        assembly.getResourceResolver().commit();

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/content/assembly1/en_US/variants/DEFAULT/draft/content/0"));
        assertNotNull(slingContext.resourceResolver().getResource("/content/assembly1/en_US/variants/DEFAULT/draft/content/1"));
        assertNull(slingContext.resourceResolver().getResource("/content/assembly1/en_US/variants/DEFAULT/draft/content/2"));

        AssemblyPage p1 = SlingModels.getModel(
                slingContext.resourceResolver().getResource("/content/assembly1/en_US/variants/DEFAULT/draft/content/0"),
                AssemblyPage.class);
        AssemblyPage p2 = SlingModels.getModel(
                slingContext.resourceResolver().getResource("/content/assembly1/en_US/variants/DEFAULT/draft/content/1"),
                AssemblyPage.class);

        assertEquals(1, p1.leveloffset().get());
        assertEquals(m1uuid, p1.module().get());
        assertEquals(0, p2.leveloffset().get());
        assertEquals(m2uuid, p2.module().get());
    }
}
