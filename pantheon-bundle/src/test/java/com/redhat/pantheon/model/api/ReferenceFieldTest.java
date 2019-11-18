package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.api.v2.Field;
import com.redhat.pantheon.model.api.v2.Reference;
import com.redhat.pantheon.model.api.v2.SlingModel;
import com.redhat.pantheon.model.api.v2.SlingModels;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Named;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({SlingContextExtension.class})
public class ReferenceFieldTest {

    SlingContext sc = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void referenceField() throws Exception {
        // Given
        sc.build()
                .resource("/test",
                        "jcr:mixinTypes", "mix:referenceable")
                .resource("/nested/referenceable",
                        "jcr:mixinTypes", "mix:referenceable")
                .commit();

        // When
        ReferenceableResource model = SlingModels.getModel(sc.resourceResolver(), "/test", ReferenceableResource.class);
        ReferenceableResource ref = SlingModels.getModel(sc.resourceResolver(), "/nested/referenceable", ReferenceableResource.class);
        model.referenceField().set(ref.jcrUUID().get());

        // Then
        assertNotNull(model.referenceField().getReference());
        assertEquals("/nested/referenceable", model.referenceField().getReference().getPath());
        assertEquals(ref.jcrUUID().get(), model.referenceField().get());
    }

    interface ReferenceableResource extends SlingModel {

        @Named(JcrConstants.JCR_UUID)
        Field<String> jcrUUID();

        Reference<ReferenceableResource> referenceField();
    }
}
