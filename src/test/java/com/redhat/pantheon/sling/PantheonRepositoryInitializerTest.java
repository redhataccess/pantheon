package com.redhat.pantheon.sling;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.internal.JcrResourceResolverFactoryImpl;
import org.apache.sling.testing.mock.sling.MockJcrSlingRepository;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.apache.sling.testing.resourceresolver.MockResourceResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Session;
import javax.jcr.security.Privilege;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class PantheonRepositoryInitializerTest {

    private final SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);
    @Mock
    private ServiceResourceResolverProvider provider;
    private final SlingRepository jcr = new MockJcrSlingRepository();


    @Test
    @DisplayName("Test that we don't have a working session during build time.")
    public void testInitializer() throws Exception {
        //Given
        Mockito.when(provider.getServiceResourceResolver()).thenReturn(slingContext.resourceResolver());
        PantheonRepositoryInitializer initializer = new PantheonRepositoryInitializer(provider);
        slingContext.create().resource("/conf/pantheon");

        //When
        initializer.processRepository(new MockJcrSlingRepository());


        //Then
        slingContext.resourceResolver().adaptTo(Session.class).checkPermission("/conf/pantheon", Privilege.JCR_READ);


    }

}
