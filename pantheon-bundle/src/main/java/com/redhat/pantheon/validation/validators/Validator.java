package com.redhat.pantheon.validation.validators;

import com.redhat.pantheon.validation.model.Violations;

import java.io.Serializable;

/**
 * The interface which all validators should implement. Each validator would decide
 *  <p>
 *      <ol>How to accept data</ol>
 *      <ol>How to implement validation logic</ol>
 *      <ol>Which data types, and/or models should be accepted for validation</ol>
 *  </p>
 *  The  commonality amongst the validators should be
 *  <p>
 *      <ol>Implement validate method</ol>
 *      <ol>The result should be an instance of {@see Violations} class</ol>
 *      <ol>Each validator should override the getName and provide its <b>unique name</b></ol>
 *  </p>
 * @author A.P. Rajshekhar
 */
public interface Validator extends Serializable {
    /**
     * Validates an object(s) by executing the validation logic
     * The implementing class provides the logic and returns the errors
     * if any, as an instance of Violations
     *
     * @return the violations the validation errors
     */
    Violations validate();

    /**
     * Gets name of the validator
     *
     * @return the name of the validator
     */
    String getName();
}
