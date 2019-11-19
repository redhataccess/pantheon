package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.api.v2.Child;
import com.redhat.pantheon.model.api.v2.Field;
import com.redhat.pantheon.model.api.v2.SlingModel;

import javax.inject.Named;

/**
 * A {@link SlingModel} which describes the structure for a module version.
 * Contains all the properties and content for the state of a given module at
 * a given time. ModuleVersions should differ in content when part of the same
 * parent, but this is not validated.
 */
@JcrPrimaryType("pant:moduleVersion")
public interface ModuleVersion extends SlingModel {

    @Named("jcr:uuid")
    Field<String> uuid();

    Child<Content> content();

    Child<Metadata> metadata();
}
