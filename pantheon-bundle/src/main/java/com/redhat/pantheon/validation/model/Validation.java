package com.redhat.pantheon.validation.model;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.workspace.WorkspaceChild;

import javax.inject.Named;


public interface Validation extends WorkspaceChild {

    @Named("pant:message")
    Field<String> message();

    @Named("pant:status")
    Field<String> status();

    @Named("pant:validationType")
    Field<String> validationType();

    default Validation setValidation (Violations violations) {
        if(null != violations.get(PantheonConstants.VALID_XREF)){
            this.message().set(violations.get(PantheonConstants.VALID_XREF).getDetails());
            this.status().set("warning");
            this.validationType().set("xref");
        }
        return this;
    }
}
