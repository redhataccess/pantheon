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

    default ModuleLocale getParentLocale() {
        return SlingModels.getModel(this.getParent().getParent(), ModuleLocale.class);
    }

}
