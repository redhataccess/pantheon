package com.redhat.pantheon.validation.model;

import java.util.*;

/**
 * Holds all the violations reported by all the executed validators
 * @author A.P. Rajshekhar
 */
public class CombinedViolations {
    private Map<String, Violations> combined = new HashMap<>();
     private ValidationClientDetails validationClientDetails = new ValidationClientDetails();
    /**
     * Add the violations reported by a validator
     *
     * @param validatorName the name of validator reporting the check failure
     * @param violations    the details of the  check failure(s)
     */
    public void add(String validatorName, Violations violations){
        //add only if validator check has failed
        if(!violations.hasViolations()) {
            return;
        }
        this.combined.put(validatorName, violations);
    }

    /**
     * Get violations based on the validator.
     *
     * @param validatorName the validator name
     * @return the violations the check failure(s)
     */
    public Violations getViolations(String validatorName){
        return this.combined.get(validatorName);
    }

    /**
     * Get all map.
     *
     * @return the map
     */
    public Map<String, Violations>getAll(){
        return Collections.unmodifiableMap(this.combined);
    }


    public ValidationClientDetails getValidationClientDetails() {
        return validationClientDetails;
    }

    public void setValidationClientDetails(ValidationClientDetails validationClientDetails) {
        this.validationClientDetails = validationClientDetails;
    }
}
