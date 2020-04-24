package com.redhat.pantheon.validation.model;

import java.util.HashMap;
import java.util.Map;

/**
 *  Container for Validation errors detected by a validator
 *  <p>
 *      If the validator does multiple checks, then there would be
 *      ErrorDetails instance for each check. The instances would be linked
 *      to the checks via violation name
 *  </p>
 */
public class Violations {
    private Map<String, ErrorDetails> errorMap = new HashMap<>();


    /**
     * Add the check done and the details, if the check failed
     *
     * @param violation name of the check done
     * @param detail    the details of the failure if check did not pass
     * @return the violations
     */
    public Violations add(String violation, ErrorDetails detail){
        errorMap.put(violation, detail);
        return this;
    }

    /**
     * Get error details.
     *
     * @param violation the name of the check for which detail has to be retrieved
     * @return the error details corresponding to the name of the check
     */
    public ErrorDetails get(String violation){
        return errorMap.get(violation);
    }

    /**
     * Returns whether the validator has recorded any failed checks
     *
     * @return true if  there are check failure else false
     */
    public boolean hasViolations(){
        return !errorMap.isEmpty();
    }
}
