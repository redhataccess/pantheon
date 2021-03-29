package com.redhat.pantheon.model.document;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.workspace.WorkspaceChild;

public interface Validations extends WorkspaceChild {
    default Child<Validation> page(int index) {
        return child(String.valueOf(index), Validation.class);
    }
}
