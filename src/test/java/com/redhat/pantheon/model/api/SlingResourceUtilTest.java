package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.Module;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class SlingResourceUtilTest {

    @Test
    void createNewSlingResource() throws Exception {
        // Given
        Resource parent = mock(Resource.class);
        Resource child = mock(Resource.class, InvocationOnMock::getMock);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        lenient().when(parent.getResourceResolver()).thenReturn(resourceResolver);
        lenient().when(child.adaptTo(eq(Node.class))).thenReturn(mock(Node.class));
        lenient().when(resourceResolver.create(eq(parent), eq("child"), anyMap()))
                .thenReturn(child);

        // When
        Module model = SlingResourceUtil.createNewSlingResource(parent, "child", Module.class);

        // Then
        assertNotNull(model);
    }

    @Test
    void createNewSlingResourcePathAlreadyExists() throws Exception {
        // Given
        Resource parent = mock(Resource.class);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        lenient().when(parent.getChild(anyString())).thenReturn(mock(Resource.class));
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
    }
}