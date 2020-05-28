package com.redhat.pantheon.model.workspace;

import com.redhat.pantheon.model.api.SlingModels;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Carlos Munoz
 */
@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class WorkspaceTest {

    private final SlingContext sc = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void createWorkspace() throws Exception {
        // Given

        // When
        Workspace ws = SlingModels.createModel(sc.resourceResolver(), "/workspace1", Workspace.class);

        // Then
        assertNotNull(ws.entities().get());
        assertNotNull(ws.moduleVariantDefinitions().get());
        assertEquals("sling:OrderedFolder", ws.moduleVariantDefinitions().get().field("jcr:primaryType", String.class).get());
        assertEquals("sling:Folder", ws.entities().get().field("jcr:primaryType", String.class).get());
    }

    /**
     * This test ensures that when creating intermediary children under the entities folder
     * they are created as folders as well. This will show up in the JCR tree nicely and will visually
     * separate actual content nodes (e.g. modules, files) from other types of nodes
     * (e.g. metadata)
     */
    @Test
    void ensureChildrenAreOfRightType() throws Exception {
        // Given
        Workspace ws = SlingModels.createModel(sc.resourceResolver(), "/workspace1", Workspace.class);

        // When
        Resource newFolder = sc.resourceResolver()
                .create(ws.entities().get(), "sub_folder", new HashMap<>());

        // Then
        assertEquals("sling:Folder", newFolder.getValueMap().get("jcr:primaryType"));
    }
}