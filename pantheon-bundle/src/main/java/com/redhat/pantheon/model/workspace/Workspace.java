package com.redhat.pantheon.model.workspace;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Folder;
import com.redhat.pantheon.model.api.OrderedFolder;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.module.ModuleVariant;

import javax.inject.Named;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Models an instance of metadata for a module. Multiple metadata
 * instances may found on a given module representing different
 * versions of said metadata.
 */
@JcrPrimaryType("pant:workspace")
public interface Workspace extends SlingModel {

    @Named("module_variants")
    Child<ModuleVariantDefinitionFolder> moduleVariantDefinitions();

    @Named("entities")
    Child<Folder> entities();

    default String getCanonicalVariantName() {
        Optional<ModuleVariantDefinition> cv = moduleVariantDefinitions().getOrCreate().getVariants().filter(def -> def.isCanonical()).findFirst();
        return cv.isPresent() ? cv.get().getName() : ModuleVariant.DEFAULT_VARIANT_NAME;
    }

    @JcrPrimaryType("sling:OrderedFolder")
    interface ModuleVariantDefinitionFolder extends OrderedFolder {

        default Stream<ModuleVariantDefinition> getVariants() {
            return this.as(ModuleVariantDefinition.class);
        }

        default Child<ModuleVariantDefinition> variant(String name) {
            return child(name, ModuleVariantDefinition.class);
        }
    }
}