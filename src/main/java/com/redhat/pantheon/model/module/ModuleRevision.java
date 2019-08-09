package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.SlingResource;
import org.apache.sling.api.resource.Resource;

/**
 * A {@link SlingResource} which describes the structure for a module revision.
 * Contains all the properties and content for the state of a given module at
 * a given time. ModuleRevisions should differ in content when part of the same
 * parent, but this is not validated.
 */
public class ModuleRevision extends SlingResource {

    public final Child<Content> content = child("content", Content.class);

    public final Child<Metadata> metadata = child("metadata", Metadata.class);

    public ModuleRevision(Resource wrapped) {
        super(wrapped);
    }
}
