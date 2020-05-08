package com.redhat.pantheon.model.workspace;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.Folder;
import com.redhat.pantheon.model.api.OrderedFolder;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

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

    @Deprecated // TODO remove this as part of the new JCR structure changes
    @Named("pant:attributeFile")
    Field<String> attributeFile();

    @Named("module_variants")
    Child<ModuleVariantDefinitionFolder> moduleVariantDefinitions();

    @Named("entities")
    Child<Folder> entities();

    @JcrPrimaryType("sling:OrderedFolder")
    interface ModuleVariantDefinitionFolder extends OrderedFolder {

        default Stream<ModuleVariantDefinition> getVariants() {
            return this.as(ModuleVariantDefinition.class);
        }

        default Optional<ModuleVariantDefinition> getVariant(String name) {
            return getVariants()
                    .filter(mvd -> name.equals(mvd.name().get()))
                    .findFirst();
        }
    }
}