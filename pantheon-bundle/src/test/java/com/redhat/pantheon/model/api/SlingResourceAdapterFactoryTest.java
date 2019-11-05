package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.module.ModuleVersion;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class SlingResourceAdapterFactoryTest {

    private final SlingContext slingContext = new SlingContext();

    SlingResourceAdapterFactory modelAdapterFactory = new SlingResourceAdapterFactory();

    @Test
    void getAdapter() {
        // Given
        slingContext.build()
                .resource("/content/module1/draft/metadata",
                        "jcr:createdBy", "a-user",
                        "jcr:created", Calendar.getInstance())
                .commit();

        // When
        ModuleVersion adapter = modelAdapterFactory.getAdapter(slingContext.resourceResolver().getResource("/content/module1/draft"),
                ModuleVersion.class);

        // Then
        assertNotNull(adapter);
        assertEquals("a-user", adapter.metadata.get().createdBy.get());
        assertNotNull(adapter.metadata.get().created.get());
    }

    @Test
    void getUnrecognizedAdapter() {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:createdBy", "a-user",
                        "jcr:created", Calendar.getInstance())
                .commit();

        // When
        String adapter = modelAdapterFactory.getAdapter(slingContext.resourceResolver().getResource("/content/module1"),
                String.class);

        // Then
        assertNull(adapter);
    }
}