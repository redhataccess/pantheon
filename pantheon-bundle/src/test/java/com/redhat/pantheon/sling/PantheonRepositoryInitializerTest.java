package com.redhat.pantheon.sling;

import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, SlingContextExtension.class})
class PantheonRepositoryInitializerTest {


    SlingContext sc = new SlingContext(ResourceResolverType.JCR_MOCK);

    @Mock
    ServiceResourceResolverProvider serviceResourceResolverProvider;

    @Test
    void processRepository() throws Exception {
        // Given
        sc.build()
                .resource("/conf/pantheon")
        .commit();
        when(serviceResourceResolverProvider.getServiceResourceResolver()).thenReturn(sc.resourceResolver());
        PantheonRepositoryInitializer pri = new PantheonRepositoryInitializer(serviceResourceResolverProvider);
        // partial mock
        pri = spy(pri);
        when(pri.getSyncServiceUrl()).thenReturn("http://localhost:8080");

        // When
        pri.processRepository(mock(SlingRepository.class));

        // Then
        assertEquals("http://localhost:8080",
                sc.resourceResolver().getResource("/conf/pantheon").getValueMap().get("pant:syncServiceUrl"));
    }

    @Test
    void processRepositoryWithoutSyncservice() throws Exception {
        // Given
        sc.build()
                .resource("/conf/pantheon")
                .commit();
        when(serviceResourceResolverProvider.getServiceResourceResolver()).thenReturn(sc.resourceResolver());
        PantheonRepositoryInitializer pri = new PantheonRepositoryInitializer(serviceResourceResolverProvider);

        // When
        pri.processRepository(mock(SlingRepository.class));

        // Then
        assertNull(sc.resourceResolver().getResource("/conf/pantheon").getValueMap().get("pant:syncServiceUrl"));
    }
}
