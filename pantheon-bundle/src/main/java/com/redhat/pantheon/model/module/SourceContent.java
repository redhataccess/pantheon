package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.Folder;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

/**
 * @author Carlos Munoz
 */
@JcrPrimaryType("sling:Folder")
public interface SourceContent extends Folder {
    Child<FileResource> draft();
    Child<FileResource> released();
}
