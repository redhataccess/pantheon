package com.redhat.pantheon.model.workspace;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Folder;
import com.redhat.pantheon.model.api.OrderedFolder;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.inject.Named;
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