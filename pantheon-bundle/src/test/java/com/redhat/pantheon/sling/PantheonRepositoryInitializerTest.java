package com.redhat.pantheon.sling;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class PantheonRepositoryInitializerTest {

    @Mock
    ServiceResourceResolverProvider serviceResourceResolverProvider;

    @Test
    void processRepository() throws Exception {
        // Given
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        Resource configNode = mock(Resource.class);
        ModifiableValueMap mvm = mock(ModifiableValueMap.class);
        when(resourceResolver.getResource(eq("/conf/pantheon"))).thenReturn(configNode);
        when(configNode.adaptTo(ModifiableValueMap.class)).thenReturn(mvm);
        when(serviceResourceResolverProvider.getServiceResourceResolver()).thenReturn(resourceResolver);
        PantheonRepositoryInitializer pri = new PantheonRepositoryInitializer(serviceResourceResolverProvider);
        // partial mock
        pri = spy(pri);
        when(pri.getSyncServiceUrl()).thenReturn("http://localhost:8080");

        // When
        pri.processRepository(mock(SlingRepository.class));

        // Then
        verify(mvm).put(eq("pant:syncServiceUrl"), eq("http://localhost:8080"));
    }

    @Test
    void processRepositoryWithoutSyncservice() throws Exception {
        // Given
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        when(serviceResourceResolverProvider.getServiceResourceResolver()).thenReturn(resourceResolver);
        PantheonRepositoryInitializer pri = new PantheonRepositoryInitializer(serviceResourceResolverProvider);

        // When
        pri.processRepository(mock(SlingRepository.class));

        // Then
        verify(resourceResolver, times(0)).getResource(any());
    }
}
