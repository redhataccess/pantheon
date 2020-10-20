package com.redhat.pantheon.extension.events.document;

import com.redhat.pantheon.model.document.DocumentVersion;

import javax.annotation.Nonnull;

/**
 * Event fired when an assembly version has been published.
 * Includes the assembly version path so it can be re-fetched in the
 * handlers if necessary.
 */
public class DocumentVersionPublishedEvent extends DocumentVersionPublishStateEvent {

    public DocumentVersionPublishedEvent(@Nonnull DocumentVersion documentVersion) {
        super(documentVersion);
    }
}
