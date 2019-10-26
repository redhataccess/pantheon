package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingResource;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import org.apache.sling.api.resource.Resource;

/**
 * A {@link SlingResource} which describes the structure for a module version.
 * Contains all the properties and content for the state of a given module at
 * a given time. ModuleVersions should differ in content when part of the same
 * parent, but this is not validated.
 */
@JcrPrimaryType("pant:moduleVersion")
public class ModuleVersion extends SlingResource {

    public final Field<String> uuid = stringField("jcr:uuid");

    public final Child<Content> content = child("content", Content.class);

    public final Child<Metadata> metadata = child("metadata", Metadata.class);

    public ModuleVersion(Resource wrapped) {
        super(wrapped);
    }
}
