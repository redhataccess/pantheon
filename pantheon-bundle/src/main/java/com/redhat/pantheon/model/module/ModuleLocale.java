package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.Folder;
import com.redhat.pantheon.model.api.OrderedFolder;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import java.util.Optional;
import java.util.stream.Stream;

import static com.redhat.pantheon.model.module.ModuleVariant.DEFAULT_VARIANT_NAME;

/**
 * A specific module locale node which houses asciidoc source and variants
 */
@JcrPrimaryType("pant:moduleLocale")
public interface ModuleLocale extends WorkspaceChild {

    Child<SourceFolder> source();

    Child<VariantsFolder> variants();

    @JcrPrimaryType("sling:Folder")
    interface SourceFolder extends Folder {
        Child<FileResource> draft();
        Child<FileResource> released();
    }

    @JcrPrimaryType("sling:OrderedFolder")
    interface VariantsFolder extends OrderedFolder {

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
}
