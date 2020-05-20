package com.redhat.pantheon.model.api.util;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.Reference;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.util.TestUtils;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Named;

import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static com.redhat.pantheon.util.TestUtils.setReferenceValue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Carlos Munoz
 */
@ExtendWith({SlingContextExtension.class})
class ResourceTraversalTest {

    SlingContext sc = new SlingContext(ResourceResolverType.JCR_OAK);

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
                .toChild(Level1::level2)
                .toChild(Level2::level3)
                .toField(Level3::name)
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
                .toChild(Level1::level2)
                .isPresent());
        assertFalse(traverseFrom(startModel)
                .toChild(Level1::level2)
                .toChild(Level2::level3)
                .isPresent());
        assertFalse(traverseFrom(startModel)
                .toChild(Level1::level2)
                .toChild(Level2::level3)
                .toField(Level3::name)
                .isPresent());
    }

    @Test
    void referenceTraversal() {
        // Given
        sc.build()
                .resource("/referenced",
                        "jcr:mixinTypes", "mix:referenceable")
                .resource("/level1")
                .commit();
        Level1 startModel = SlingModels.getModel(sc.resourceResolver().getResource("/level1"), Level1.class);
        registerMockAdapter(Level1.class, sc);
        setReferenceValue(startModel, "ref", sc.resourceResolver().getResource("/referenced"));

        // When

        // Then
        assertTrue(traverseFrom(startModel)
                .toRef(Level1::ref)
                .isPresent()
        );
        assertNotEquals("", traverseFrom(startModel)
                .toRef(Level1::ref)
                .toField(Referenced::uuid)
                .get()
        );
    }

    @Test
    void incompleteReferenceTraversal() {
        // Given
        sc.build()
//                .resource("/referenced",
//                        "jcr:mixinTypes", "mix:referenceable")
                .resource("/level1")
                .commit();
        Level1 startModel = SlingModels.getModel(sc.resourceResolver().getResource("/level1"), Level1.class);
        registerMockAdapter(Level1.class, sc);
//        setReferenceValue(startModel, "ref", sc.resourceResolver().getResource("/referenced"));

        // When

        // Then
        assertFalse(traverseFrom(startModel)
                .toRef(Level1::ref)
                .isPresent());
    }

    interface Level1 extends SlingModel {
        Child<Level2> level2();

        Reference<Referenced> ref();
    }

    interface Level2 extends SlingModel {
        Child<Level3> level3();
    }

    interface Level3 extends SlingModel {
        Field<String> name();
    }

    interface Referenced extends SlingModel {
        @Named("jcr:uuid")
        Field<String> uuid();
    }
}