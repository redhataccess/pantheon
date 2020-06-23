package com.redhat.pantheon.model.Document;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.module.SourceContent;
import com.redhat.pantheon.model.module.Variants;

public interface DocumentLocale extends WorkspaceChild {
    Child<SourceContent> source();

    Child<Variants> variants();

    Document getParent();
}
