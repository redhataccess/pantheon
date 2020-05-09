package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

/**
 * A specific module locale node which houses asciidoc source and variants.
 * A locale will contain two folders to store the source content (draft and released),
 * and the different variants (the rendered content)
 */
@JcrPrimaryType("pant:moduleLocale")
public interface ModuleLocale extends WorkspaceChild {

    Child<SourceContent> source();

    Child<Variants> variants();
}
