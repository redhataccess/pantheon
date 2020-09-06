package com.redhat.pantheon.model.document;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.workspace.WorkspaceChild;

import javax.inject.Named;

/**
 * A {@link SlingModel} which describes the structure for a Document version.
 * Contains all the properties and content for the state of a given Document at
 * a given time. DocumentVersions should differ in content when part of the same
 * parent, but this is not validated.
 */
public interface DocumentVersion extends WorkspaceChild {

    @Named("jcr:uuid")
    Field<String> uuid();

    @Named("pant:hash")
    Field<String> hash();

    @Named("cached_html")
    Child<FileResource> cachedHtml();

    Child<? extends DocumentMetadata> metadata();

    @Named("ack_status")
    Child<AckStatus> ackStatus();

    @Override
    DocumentVariant getParent();
}
