package com.redhat.pantheon.validation.model;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.workspace.WorkspaceChild;
import com.redhat.pantheon.validation.helper.XrefValidationHelper;
import org.apache.sling.api.resource.ValueMap;

import javax.inject.Named;

@JcrPrimaryType("pant:validation")
public interface Validation extends WorkspaceChild {

    @Named("pant:message")
    Field<String> message();

    @Named("pant:status")
    Field<String> status();

    @Named("pant:validationType")
    Field<String> validationType();

    @Named("pant:xrefTarget")
    Field<String> xrefTarget();

    default Validation setValidation (Violations violations, int index) {
        if(null != violations.get(PantheonConstants.TYPE_XREF)){
            this.message().set("Invalid cross references");
            this.status().set("error");
            this.validationType().set("xref");
            this.xrefTarget().set(violations.get(PantheonConstants.TYPE_XREF).getDetails(index));
        }
        return this;
    }
    default  Validation getValidation(){
        return this;
    }
}
