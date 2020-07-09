package com.redhat.pantheon.model.document;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.module.SourceContent;

public interface DocumentLocale<T extends DocumentVariants> extends WorkspaceChild {
    Child<SourceContent> getSource();

    Child<T> getVariants();

    Document getParent();
}
