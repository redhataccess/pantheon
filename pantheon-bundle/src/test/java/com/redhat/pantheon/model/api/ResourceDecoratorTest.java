package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Carlos Munoz
 */
@ExtendWith({MockitoExtension.class})
class ResourceDecoratorTest {

    @Mock
    Resource resource;

    @Test
    void getProperty() {
        // Given
        ValueMap valueMap = mock(ValueMap.class);
        when(valueMap.get(eq("prop"), eq(String.class))).thenReturn("propval");
        when(resource.getValueMap()).thenReturn(valueMap);

        // When
        ResourceDecorator decorator = new ResourceDecorator(resource);

        // Then
        assertEquals("propval", decorator.getProperty("prop", String.class));
    }

    @Test
    void setProperty() {
        // Given
        ModifiableValueMap valueMap = mock(ModifiableValueMap.class);
        when(resource.adaptTo(eq(ModifiableValueMap.class))).thenReturn(valueMap);

        // When
        ResourceDecorator decorator = new ResourceDecorator(resource);
        decorator.setProperty("prop", "propval");

        // Then
        verify(valueMap, times(1)).put(eq("prop"), eq("propval"));
    }

    @Test
    void delete() throws Exception {
        // Given
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        when(resource.getResourceResolver()).thenReturn(resourceResolver);

        // When
        ResourceDecorator decorator = new ResourceDecorator(resource);
        decorator.delete();

        // Then
        verify(resourceResolver, times(1)).delete(eq(decorator));
    }

    @Test
    void listChildren() {
        // When
        new ResourceDecorator(resource).listChildren();

        // Then
        verify(resource, times(1)).listChildren();
    }

    @Test
    void getResourceType() {
        // When
        new ResourceDecorator(resource).getResourceType();

        // Then
        verify(resource, times(1)).getResourceType();
    }

    @Test
    void getResourceSuperType() {
        // When
        new ResourceDecorator(resource).getResourceSuperType();

        // Then
        verify(resource, times(1)).getResourceSuperType();
    }

    @Test
    void hasChildren() {
        // When
        new ResourceDecorator(resource).hasChildren();

        // Then
        verify(resource, times(1)).hasChildren();
    }

    @Test
    void isResourceType() {
        // Given
        when(resource.isResourceType(anyString())).thenReturn(true);

        // When
        new ResourceDecorator(resource).isResourceType("resourcetype");

        // Then
        verify(resource, times(1)).isResourceType(eq("resourcetype"));
    }

    @Test
    void getResourceMetadata() {
        // When
        new ResourceDecorator(resource).getResourceMetadata();

        // Then
        verify(resource, times(1)).getResourceMetadata();
    }
}
