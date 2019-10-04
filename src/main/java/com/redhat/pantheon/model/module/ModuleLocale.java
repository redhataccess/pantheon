package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.ReferenceField;
import com.redhat.pantheon.model.api.SlingResource;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import org.apache.sling.api.resource.Resource;

import javax.annotation.Nonnull;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.counting;

/**
 * A specific module locale node which houses all the versions for a specific language in the module.
 */
@JcrPrimaryType("pant:moduleLocale")
public class ModuleLocale extends SlingResource {

    public final ReferenceField<ModuleVersion> released = referenceField("released", ModuleVersion.class);

    public final ReferenceField<ModuleVersion> draft = referenceField("draft", ModuleVersion.class);

    public ModuleLocale(@Nonnull Resource resource) {
        super(resource);
    }

    public ModuleVersion getVersion(String name) {
        return child(name, ModuleVersion.class).get();
    }

    public ModuleVersion getOrCreateVersion(String name) {
        return child(name, ModuleVersion.class).getOrCreate();
    }

    public ModuleVersion createNextVersion() {
        // Generate a new version name
        return child(generateNextVersionName(), ModuleVersion.class).create();
    }

    private String generateNextVersionName() {
        return "" + (stream(this.getChildren()).collect(counting()) + 1);
    }
}
