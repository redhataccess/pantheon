package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.Product;
import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.module.Content;
import com.redhat.pantheon.model.module.ModuleMetadata;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.model.module.ModuleVersion;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class SlingModelAdapterFactoryTest {

    private final SlingContext slingContext = new SlingContext();

    SlingModelAdapterFactory modelAdapterFactory = new SlingModelAdapterFactory();

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
        assertEquals("a-user", adapter.metadata().get().createdBy().get());
        assertNotNull(adapter.metadata().get().created().get());
    }

    // Make sure there are no regressions that make adapters unavailable
    @Test
    void availableAdapters() {
        // Given
        Resource mockResource = Mockito.mock(Resource.class);

        // Then
        assertNotNull(modelAdapterFactory.getAdapter(mockResource, SlingModel.class));
        assertNotNull(modelAdapterFactory.getAdapter(mockResource, Module.class));
        assertNotNull(modelAdapterFactory.getAdapter(mockResource, ModuleVersion.class));
        assertNotNull(modelAdapterFactory.getAdapter(mockResource, Content.class));
        assertNotNull(modelAdapterFactory.getAdapter(mockResource, ModuleMetadata.class));
        assertNotNull(modelAdapterFactory.getAdapter(mockResource, ModuleVariant.class));
        assertNotNull(modelAdapterFactory.getAdapter(mockResource, Product.class));
        assertNotNull(modelAdapterFactory.getAdapter(mockResource, ProductVersion.class));
        assertNotNull(modelAdapterFactory.getAdapter(mockResource, FileResource.class));

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

    @Test
    void getUnrecognizedAdaptable() {
        // When
        assertNull(modelAdapterFactory.getAdapter("A string", Module.class));
    }
}
