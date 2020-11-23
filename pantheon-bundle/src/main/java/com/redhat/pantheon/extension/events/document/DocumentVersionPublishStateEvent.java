package com.redhat.pantheon.extension.events.document;

import com.redhat.pantheon.extension.Event;
import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.document.DocumentVersion;

import javax.annotation.Nonnull;

/**
 * Event fired when a assembly version has been published.
 * Includes the assembly version path so it can be re-fetched in the
 * handlers if necessary.
 */
public class DocumentVersionPublishStateEvent implements Event {

    private final String documentVersionPath;

    protected DocumentVersionPublishStateEvent(@Nonnull DocumentVersion documentVersion) {
        this.documentVersionPath = documentVersion.getPath();
    }

    public String getDocumentVersionPath() {
        return documentVersionPath;
    }
}
