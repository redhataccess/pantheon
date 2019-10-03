package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.ReferenceField;
import com.redhat.pantheon.model.api.SlingResource;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import org.apache.sling.api.resource.Resource;

import javax.annotation.Nonnull;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.counting;

/**
 * A specific module locale node which houses all the revisions for a specific language in the module.
 */
@JcrPrimaryType("pant:moduleLocale")
public class ModuleLocale extends SlingResource {

    public final ReferenceField<ModuleRevision> released = referenceField("released", ModuleRevision.class);

    public final ReferenceField<ModuleRevision> draft = referenceField("draft", ModuleRevision.class);

    public ModuleLocale(@Nonnull Resource resource) {
        super(resource);
    }

    public ModuleRevision getRevision(String name) {
        return child(name, ModuleRevision.class).get();
    }

    public ModuleRevision getOrCreateRevision(String name) {
        return child(name, ModuleRevision.class).getOrCreate();
    }

    public ModuleRevision createNextRevision() {
        // Generate a new revision name
        return child(generateNextRevisionName(), ModuleRevision.class).create();
    }

    private String generateNextRevisionName() {
        return "" + (stream(this.getChildren()).collect(counting()) + 1);
    }
}
