package com.redhat.pantheon.model.api;

import com.google.common.collect.Maps;
import com.redhat.pantheon.model.Module;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class SlingResourceUtilTest {

    @Test
    void createNewSlingResource() throws Exception {
        // Given
        Resource parent = mock(Resource.class);
        Resource child = mock(Resource.class);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        lenient().when(parent.getResourceResolver()).thenReturn(resourceResolver);
        lenient().when(child.getValueMap()).thenReturn(mock(ValueMap.class));
        lenient().when(resourceResolver.create(eq(parent), eq("child"), anyMap()))
                .thenReturn(child);

        // When
        Module model = SlingResourceUtil.createNewSlingResource(parent, "child", Module.class);

        // Then
        assertNotNull(model);
        assertEquals(child, model.getResource());
    }

    @Test
    void createNewSlingResourcePathAlreadyExists() throws Exception {
        // Given
        Resource parent = mock(Resource.class);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        lenient().when(parent.getResourceResolver()).thenReturn(resourceResolver);

        // When

        // Then
        assertThrows(RuntimeException.class,
                () -> SlingResourceUtil.createNewSlingResource(parent, "child", Module.class));
    }

    @Test
    void toSlingResource() {
        // Given
        Resource resource = mock(Resource.class);

        // When
        Module model = SlingResourceUtil.toSlingResource(resource, Module.class);

        // Then
        assertNotNull(model);
        assertEquals(resource, model.getResource());
    }
}