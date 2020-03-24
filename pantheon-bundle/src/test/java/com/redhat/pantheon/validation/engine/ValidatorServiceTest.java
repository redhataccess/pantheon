package com.redhat.pantheon.validation.engine;

import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.Module;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.jcr.Node;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class ValidatorServiceTest {
    private final SlingContext slingContext = new SlingContext();
    private Module module;
    private BundleContext bundleContext;
    @BeforeEach
    public void setup() throws IOException {
        slingContext.build()
                .resource("/content/module1")
                .commit();
        slingContext.registerAdapter(Resource.class, Node.class, mock(Node.class));
        module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/module1"),
                        Module.class);
        bundleContext = mock(BundleContext.class);
        InputStream stubInputStream =
                IOUtils.toInputStream("module.icluded=\"Module cannot include another module\"", "UTF-8");
        Bundle bundle = mock(Bundle.class);
        lenient().when(bundleContext.getBundle()).thenReturn(bundle);
        URL url = getClass().getResource("/validation-messages.properties");
        //lenient().when(url.openStream()).thenReturn(stubInputStream);
        when(bundle.getResource("validation-messages.properties")).thenReturn(url);
        //lenient().when(bundleContext.getBundle().getResource("validation-messages.properties").openStream()).thenReturn(stubInputStream);
        lenient().doReturn(url).when(bundle).getResource("");
       // lenient().doReturn(stubInputStream).when(url).openStream();
    }

    @Test
    public void testValidationServiceWhenModuleIsNotIncludedInModule(){
        ValidatorService validatorService = new ValidatorService();
        validatorService.setBundleContext(bundleContext);
        validatorService.postConstruct();
       Assertions.assertTrue(validatorService.validate(module).isEmpty());
    }

    @Test
    public void testValidationServiceWhenModuleIsIncludedInModule(){
        module.createChild("childModule", Module.class);
        ValidatorService validatorService = new ValidatorService();
        validatorService.setBundleContext(bundleContext);
        validatorService.postConstruct();
        validatorService.validate(module);
        Assertions.assertFalse(validatorService.validate(module).isEmpty());
    }

}
