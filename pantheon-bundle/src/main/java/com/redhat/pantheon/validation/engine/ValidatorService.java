package com.redhat.pantheon.validation.engine;

import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.validation.validators.ModuleIncludeValidator;
import com.redhat.pantheon.validation.validators.ValidationMessageHelper;
import com.redhat.pantheon.validation.validators.Validator;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Business service class that provides validation services.
 */
@Component(
        service = ValidatorService.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Provides validation services",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })

public class ValidatorService {

    private BundleContext bundleContext;
    private   HashMap<Class, Validator> map;
    /**
     * Calls the validator based on the model and returns zero or more  error messages as a list
     * @param model sling model to be validated
     */
    public List<String> validate(SlingModel model){
        //get interfaces returns array of interfaces implemented by the model
        //[0] will contain very first interface. For example, dynamic proxy implements
        // Module that in turn implements SlingModel. Hence, element at [0] will be Module
        return map.get(model.getClass().getInterfaces()[0]).validate(model);
    }

    /**
     * The context of the current bundle. This is required to initialize
     * the validators
     * @return
     */
    public BundleContext getBundleContext() {
        if(bundleContext == null) {
            return FrameworkUtil.getBundle(
                    this.getClass()).getBundleContext();
        }
        return bundleContext;
    }
    public void setBundleContext(BundleContext bundleContext){
        this.bundleContext = bundleContext;
    }

    @Activate
    public void postConstruct(){
        map = new HashMap<Class, Validator>(){
            {
                put(Module.class, new ModuleIncludeValidator(new ValidationMessageHelper(getBundleContext())));
            }
        };
    }
}
