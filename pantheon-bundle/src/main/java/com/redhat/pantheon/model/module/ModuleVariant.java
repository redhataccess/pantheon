package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.document.DocumentVariant;
import org.apache.sling.api.resource.PersistenceException;

import javax.jcr.RepositoryException;

import java.util.Calendar;

import static com.google.common.collect.Streams.stream;
import static com.redhat.pantheon.jcr.JcrResources.rename;

/**
 * A specific module variant node which houses all the versions for a specific language in the module.
 */
@JcrPrimaryType("pant:moduleVariant")
public interface ModuleVariant extends DocumentVariant {

    String DEFAULT_VARIANT_NAME = "DEFAULT";

    Child<ModuleVersion> draft();

    Child<ModuleVersion> released();

    @Override
    ModuleVariants getParent();

    // Since we are not storing historical versions anymore, the only needed ones are draft and released
    /*default ModuleVersion getVersion(String name) {
        return getChild(name, ModuleVersion.class);
    }*/

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
