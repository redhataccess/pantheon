package com.redhat.pantheon.validation.validators;

import java.util.List;

/**
 * Base class for validators.
 * A validator would extend this class and implement validate method
 * as per requirement. The logic of validation is left to the implementing class.
 * The implementing class can call external tools or services if needed
 * @param <T> Model to be validated
 *
 */
public abstract class Validator<T> {
    /**
     * Provides messages related with validation based on the key
     */
    private ValidationMessageHelper validationMessageHelper;

    public  Validator(ValidationMessageHelper validationMessageHelper){
        setValidationMessageHelper(validationMessageHelper);
    }

    /**
     * Validates the object and retuns the error messages as a {@link java.util.List}
     * @param objectToValidate
     * @return list of zero or more validation error messages
     */
    public abstract List<String> validate(T objectToValidate);

    protected ValidationMessageHelper getValidationMessageHelper() {
        return validationMessageHelper;
    }

    public void setValidationMessageHelper(ValidationMessageHelper validationMessageHelper) {
        this.validationMessageHelper = validationMessageHelper;
    }
}
