package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.document.DocumentLocale;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.document.SourceContent;

/**
 * A specific module locale node which houses asciidoc source and variants.
 * A locale will contain two folders to store the source content (draft and released),
 * and the different variants (the rendered content)
 */
@JcrPrimaryType("pant:moduleLocale")
public interface ModuleLocale extends DocumentLocale {

    @Override
    Child<ModuleVariants> variants();

    @Override
    Module getParent();
}
