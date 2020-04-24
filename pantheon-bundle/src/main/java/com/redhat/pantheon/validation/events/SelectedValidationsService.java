package com.redhat.pantheon.validation.events;


import com.redhat.pantheon.validation.model.ValidationClientDetails;
import com.redhat.pantheon.validation.validators.Validator;
import org.osgi.service.component.annotations.Component;

import java.util.List;

/**
 * Holds the instances of validators that the client wants to execute
 * asynchronously. The client would inject an instance of this class
 * and add the validator instances. {@see ValidationTriggerProcessor}
 * would then use its injected instance of this class to retrieve the validator
 * instances.
 */
@Component(service = SelectedValidationsService.class)
public class SelectedValidationsService {
    private List<Validator> validators;
    private ValidationClientDetails validationClientDetails;

    /**
     * Instantiates a new Selected validations service.
     */
    public SelectedValidationsService(){}

    /**
     * Instantiates a new Selected validations service.
     *
     * @param validators              the validators that need to be executed asynchronously
     * @param validationClientDetails the details of the client requesting validation
     */
    public SelectedValidationsService(List<Validator> validators, ValidationClientDetails validationClientDetails) {
        this.validators = validators;
        this.setValidationClientDetails(validationClientDetails);
    }

    /**
     * Gets validators.
     *
     * @return the validators
     */
    public List<Validator> getValidators() {
        return validators;
    }

    /**
     * Sets validators.
     *
     * @param validators the validators
     */
    public void setValidators(List<Validator> validators) {
        this.validators = validators;
    }

    /**
     * Gets validation client details.
     *
     * @return the validation client details
     */
    public ValidationClientDetails getValidationClientDetails() {
        return validationClientDetails;
    }

    /**
     * Sets validation client details.
     *
     * @param validationClientDetails the validation client details
     */
    public void setValidationClientDetails(ValidationClientDetails validationClientDetails) {
        this.validationClientDetails = validationClientDetails;
    }
}
