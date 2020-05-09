package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.OrderedFolder;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import java.util.Optional;
import java.util.stream.Stream;

import static com.redhat.pantheon.model.module.ModuleVariant.DEFAULT_VARIANT_NAME;

/**
 * @author Carlos Munoz
 */
@JcrPrimaryType("sling:OrderedFolder")
public interface Variants extends OrderedFolder {

    default Stream<ModuleVariant> getVariants() {
        return this.as(ModuleVariant.class);
    }

    default ModuleVariant getOrCreateVariant(String name) {
        return getOrCreateChild(name, ModuleVariant.class);
    }

    default ModuleVariant getOrCreateVariant() {
        return getOrCreateVariant(DEFAULT_VARIANT_NAME);
    }

    default Optional<ModuleVariant> getVariant(String name) {
        return Optional.ofNullable(getChild(name, ModuleVariant.class));
    }
}
