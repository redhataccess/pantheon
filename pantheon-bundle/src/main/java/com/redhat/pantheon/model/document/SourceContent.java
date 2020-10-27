package com.redhat.pantheon.model.document;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Folder;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.HashableFileResource;

/**
 * @author Carlos Munoz
 */
@JcrPrimaryType("sling:Folder")
public interface SourceContent extends Folder {
    Child<HashableFileResource> draft();
    Child<HashableFileResource> released();

    default Child<HashableFileResource> latest() {
        return draft().get() == null ? released() : draft();
    }
}
