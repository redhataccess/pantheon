package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.document.DocumentVariant;

/**
 * A specific assembly variant node which houses all the versions for a specific language in the assembly.
 */
@JcrPrimaryType("pant:assemblyVariant")
public interface AssemblyVariant extends DocumentVariant {

    Child<AssemblyVersion> draft();

    Child<AssemblyVersion> released();

    @Override
    AssemblyVariants getParent();

    // TODO Not sure we need this
    default AssemblyVersion getOrCreateVersion(String name) {
        return child(name, AssemblyVersion.class).get();
    }

    // TODO Not sure we need this
    default AssemblyVersion createNextVersion() {
        // Generate a new version name
        return child(generateNextVersionName(), AssemblyVersion.class).create();
    }

    default AssemblyLocale getParentLocale() {
        return SlingModels.getModel(this.getParent().getParent(), AssemblyLocale.class);
    }

}
