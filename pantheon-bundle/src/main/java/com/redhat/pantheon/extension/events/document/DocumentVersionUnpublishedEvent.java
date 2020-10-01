package com.redhat.pantheon.extension.events.document;

import com.redhat.pantheon.model.document.DocumentVersion;

import javax.annotation.Nonnull;

/**
 * Event fired when a assembly version has been published.
 * Includes the assembly version path so it can be re-fetched in the
 * handlers if necessary.
 */
public class DocumentVersionUnpublishedEvent extends DocumentVersionPublishStateEvent {

    private final String publishedUrl;

    public DocumentVersionUnpublishedEvent(@Nonnull DocumentVersion documentVersion, String publishedUrl) {
        super(documentVersion);
        this.publishedUrl = publishedUrl;
    }

    public String getPublishedUrl() {
        return publishedUrl;
    }
}
