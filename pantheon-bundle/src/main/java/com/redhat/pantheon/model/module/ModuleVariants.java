package com.redhat.pantheon.model.module;

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
public interface ModuleVariants extends DocumentVariants {

    // The default variant
    @Named(DEFAULT_VARIANT_NAME)
    Child<ModuleVariant> defaultVariant();

    default Child<ModuleVariant> canonicalVariant() {
        return variant(getParent().getWorkspace().getCanonicalVariantName());
    }

    default Stream<ModuleVariant> getVariants() {
        return this.as(ModuleVariant.class);
    }

    default Child<ModuleVariant> variant(String name) {
        return child(name, ModuleVariant.class);
    }

    @Override
    ModuleLocale getParent();
}
