package com.redhat.pantheon.validation.model;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.workspace.WorkspaceChild;

@JcrPrimaryType("sling:OrderedFolder")
public interface Validations extends WorkspaceChild {
    default Child<Validation> page(String type) {
        return child(type, Validation.class);
    }
}
