package com.redhat.pantheon.validation.validators;

import com.redhat.pantheon.model.module.Module;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.List;

public class ModuleIncludeValidator extends Validator<Module> {


    public ModuleIncludeValidator(ValidationMessageHelper validationMessageHelper){
        super(validationMessageHelper);
    }

    @Override
    public List<String> validate(Module module) {
        return checkIfModuleIncludesModule(module, new ArrayList<>());
    }

    private List<String> checkIfModuleIncludesModule(Module module, List<String> validationErrors) {
        for (Resource resource:  module.getChildren()) {
            if(resource.getResourceType().equalsIgnoreCase("pant:module")){
                validationErrors.add(getValidationMessageHelper().getMessage("module.included"));
                break;
            }
        }
        return validationErrors;
    }
}
