package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import org.apache.sling.api.resource.Resource;

import javax.inject.Named;

/**
 * A {@link SlingModel} which describes the structure for a module version.
 * Contains all the properties and content for the state of a given module at
 * a given time. ModuleVersions should differ in content when part of the same
 * parent, but this is not validated.
 */
@JcrPrimaryType("pant:moduleVersion")
public interface ModuleVersion extends WorkspaceChild {

    @Named("jcr:uuid")
    Field<String> uuid();

    @Named("pant:hash")
    Field<String> hash();

    @Named("cached_html")
    Child<FileResource> cachedHtml();

    Child<Metadata> metadata();

    @Named("ack_status")
    Child<AckStatus> ackStatus();

    @Override
    ModuleVariant getParent();
}
