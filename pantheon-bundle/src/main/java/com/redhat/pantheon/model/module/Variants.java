package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.OrderedFolder;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.inject.Named;
import java.util.Optional;
import java.util.stream.Stream;

import static com.redhat.pantheon.model.module.ModuleVariant.DEFAULT_VARIANT_NAME;

/**
 * @author Carlos Munoz
 */
@JcrPrimaryType("sling:OrderedFolder")
public interface Variants extends OrderedFolder {

    // The default variant
    @Named(DEFAULT_VARIANT_NAME)
    Child<ModuleVariant> defaultVariant();

    default Stream<ModuleVariant> getVariants() {
        return this.as(ModuleVariant.class);
    }

    default ModuleVariant getOrCreateVariant(String name) {
        return getOrCreateChild(name, ModuleVariant.class);
    }

    default Optional<ModuleVariant> getVariant(String name) {
        return Optional.ofNullable(child(name, ModuleVariant.class).get());
    }

    @Override
    ModuleLocale getParent();
}
