package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.document.DocumentVariant;

/**
 * A specific module variant node which houses all the versions for a specific language in the module.
 */
@JcrPrimaryType("pant:moduleVariant")
public interface ModuleVariant extends DocumentVariant {

    Child<ModuleVersion> draft();

    Child<ModuleVersion> released();

    @Override
    ModuleVariants getParent();

    // TODO Not sure we need this
    default ModuleVersion getOrCreateVersion(String name) {
        return child(name, ModuleVersion.class).get();
    }

    // TODO Not sure we need this
    default ModuleVersion createNextVersion() {
        // Generate a new version name
        return child(generateNextVersionName(), ModuleVersion.class).create();
    }

    default ModuleLocale getParentLocale() {
        return SlingModels.getModel(this.getParent().getParent(), ModuleLocale.class);
    }

}
