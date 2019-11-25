package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.api.Reference;
import com.redhat.pantheon.model.api.SlingModel;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.counting;

/**
 * A specific module locale node which houses all the versions for a specific language in the module.
 */
@JcrPrimaryType("pant:moduleLocale")
public interface ModuleLocale extends SlingModel {

    Reference<ModuleVersion> released();

    Reference<ModuleVersion> draft();

    default ModuleVersion getVersion(String name) {
        return getChild(name, ModuleVersion.class);
    }

    default ModuleVersion getOrCreateVersion(String name) {
        return getOrCreateChild(name, ModuleVersion.class);
    }

    default ModuleVersion createNextVersion() {
        // Generate a new version name
        return createChild(generateNextVersionName(), ModuleVersion.class);
    }

    default String generateNextVersionName() {
        return "" + (stream(this.getChildren()).collect(counting()) + 1);
    }
}
