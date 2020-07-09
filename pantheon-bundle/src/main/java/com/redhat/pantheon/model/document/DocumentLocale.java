package com.redhat.pantheon.model.document;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.WorkspaceChild;

public interface DocumentLocale extends WorkspaceChild {
    Child<SourceContent> getSource();

    Child<? extends DocumentVariants> variants();

    @Override
    Document getParent();
}
