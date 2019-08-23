package com.redhat.pantheon.sling;

import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Session;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import static org.mockito.Mockito.mock;


@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class PantheonRepositoryInitializerTest {

    private final SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);
    @Mock
    private ServiceResourceResolverProvider provider;
    @Mock
    private SlingRepository jcr;

    @Test
    @DisplayName("Test that we don't get a NPE when checking permissions.")
    public void testInitializer() throws Exception {
        //Given
        PantheonRepositoryInitializer initializer = mock(PantheonRepositoryInitializer.class);
        slingContext.create().resource("/conf/pantheon");
        //doNothing().when(initializer).processRepository(isA(SlingRepository.class));

        //When
        initializer.processRepository(jcr);

        //Then
       // assertThrows(NullPointerException.class, () -> initializer.processRepository(jcr));

        assertDoesNotThrow(() -> slingContext.resourceResolver().adaptTo(Session.class).checkPermission("/conf/pantheon", "set_property"));
    }

}
