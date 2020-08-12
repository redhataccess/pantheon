package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.document.DocumentVariants;

import javax.inject.Named;
import java.util.stream.Stream;

import static com.redhat.pantheon.model.document.DocumentVariant.DEFAULT_VARIANT_NAME;

/**
 * @author Carlos Munoz
 */
@JcrPrimaryType("sling:OrderedFolder")
public interface AssemblyVariants extends DocumentVariants {

    // The default variant
    @Named(DEFAULT_VARIANT_NAME)
    Child<AssemblyVariant> defaultVariant();

    default Child<AssemblyVariant> canonicalVariant() {
        return variant(getParent().getWorkspace().getCanonicalVariantName());
    }

    default Stream<AssemblyVariant> getVariants() {
        return this.as(AssemblyVariant.class);
    }

    default Child<AssemblyVariant> variant(String name) {
        return child(name, AssemblyVariant.class);
    }

    @Override
    AssemblyLocale getParent();
}
