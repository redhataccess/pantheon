package com.redhat.pantheon.validation.model;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.workspace.WorkspaceChild;

import javax.inject.Named;

@JcrPrimaryType("sling:OrderedFolder")
public interface ValidationType extends WorkspaceChild {

    default Child<Validation> page(int index) {
        return child(String.valueOf(index), Validation.class);
    }
}
