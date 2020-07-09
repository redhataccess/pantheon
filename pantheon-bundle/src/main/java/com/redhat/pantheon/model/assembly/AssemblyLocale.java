package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.model.document.DocumentLocale;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

/**
 * A specific assembly locale node which houses asciidoc source and variants.
 * A locale will contain two folders to store the source content (draft and released),
 * and the different variants (the rendered content)
 */
@JcrPrimaryType("pant:assemblyLocale")
public interface AssemblyLocale extends DocumentLocale {

    @Override
    Assembly getParent();
}
