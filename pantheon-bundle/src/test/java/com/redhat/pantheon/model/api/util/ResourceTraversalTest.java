package com.redhat.pantheon.model.api.util;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.SlingModels;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Carlos Munoz
 */
@ExtendWith({SlingContextExtension.class})
class ResourceTraversalTest {

    SlingContext sc = new SlingContext();

    @Test
    void traverse() {
        // Given
        sc.build()
                .resource("/level1/level2/level3",
                        "name", "A NAME")
                .commit();
        Level1 startModel = SlingModels.getModel(sc.resourceResolver().getResource("/level1"), Level1.class);

        // When

        // Then
        assertEquals("A NAME", traverseFrom(startModel)
                .traverse(Level1::level2)
                .traverse(Level2::level3)
                .field(Level3::name)
                .get());
    }

    @Test
    void incompleteTraversal() {
        // Given
        sc.build()
                .resource("/level1")
                .commit();
        Level1 startModel = SlingModels.getModel(sc.resourceResolver().getResource("/level1"), Level1.class);

        // When

        // Then
        assertFalse(traverseFrom(startModel)
                .traverse(Level1::level2)
                .isPresent());
        assertFalse(traverseFrom(startModel)
                .traverse(Level1::level2)
                .traverse(Level2::level3)
                .isPresent());
        assertFalse(traverseFrom(startModel)
                .traverse(Level1::level2)
                .traverse(Level2::level3)
                .field(Level3::name)
                .isPresent());
    }

    interface Level1 extends SlingModel {
        Child<Level2> level2();
    }

    interface Level2 extends SlingModel {
        Child<Level3> level3();
    }

    interface Level3 extends SlingModel {
        Field<String> name();
    }
}