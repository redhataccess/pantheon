package com.redhat.pantheon.validation.validators;

import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.Module;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;

import javax.jcr.Node;

import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class ModuleIncludeValidatorTest {
    private final SlingContext slingContext = new SlingContext();
    private Module module;
    @Mock
    private BundleContext bundleContext;

   private ValidationMessageHelper validationMessageHelper;

    ModuleIncludeValidator moduleIncludeValidator;
    @BeforeEach
    public void setup(){
        MockitoAnnotations.initMocks(this);
        validationMessageHelper = mock(ValidationMessageHelper.class);
        lenient().when(validationMessageHelper.getMessage("test")).thenReturn("test");
        slingContext.build()
                .resource("/content/module1")
                .commit();
        slingContext.registerAdapter(Resource.class, Node.class, mock(Node.class));
        module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/module1"),
                        Module.class);
    }

    @Test
    public void testModuleIncludeWhenModuleIsNotIncluded(){
        moduleIncludeValidator= new ModuleIncludeValidator(validationMessageHelper);

        Assertions.assertTrue(moduleIncludeValidator.validate(module).isEmpty());
    }
    @Test
    public void testModuleIncludeWhenModuleIsIncluded(){
        module.createChild("childModule", Module.class);
        moduleIncludeValidator = new ModuleIncludeValidator(validationMessageHelper);
        //moduleIncludeValidator.setModule(module);
        Assertions.assertFalse(moduleIncludeValidator.validate(module).isEmpty());
    }
}
