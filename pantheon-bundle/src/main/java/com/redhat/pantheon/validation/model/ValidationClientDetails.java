package com.redhat.pantheon.validation.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;

/**
 * Holds the details of the component/caller of the validator. This would come into
 *  picture only when event based validation is used
 */
public class ValidationClientDetails implements Comparable<String>, Serializable {
    private String componentName;
    private Date when;

    /**
     * Instantiates a new Validation client details.
     */
    public ValidationClientDetails(){}

    /**
     * Instantiates a new Validation client details with name of the component/ caller
     *  that requested the validation
     *
     * @param componentName the component name
     */
    public ValidationClientDetails(String componentName) {
        this.componentName = componentName;
    }

    /**
     * Instantiates a new Validation client details with caller/component name
     * and the date time when the validation was requested
     *
     * @param componentName the component name
     * @param when          the date time when the validation(s) were requested
     */
    public ValidationClientDetails(String componentName, Date when) {
        this.componentName = componentName;
        this.when = when;
    }

    @Override
    public int compareTo(@NotNull String other) {
        return componentName.compareTo(other);
    }
}
