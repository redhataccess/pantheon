package com.redhat.pantheon.validation.events;

import com.redhat.pantheon.extension.Event;

/**
 * This event triggers the validators to be executed
 *
 * @author A.P. Rajshekhar
 */
public class ValidationTrigger implements Event {

    private String names;
    private String component;
    /**
     * Instantiates a new Validation trigger event with name of validators as payload
     *
     * @param names the validators that need to be executed
     */
    public ValidationTrigger(String names) {
        this.setNames(names);
    }

    public ValidationTrigger(String names, String component) {
        this.names = names;
        this.setComponent(component);
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }
}
