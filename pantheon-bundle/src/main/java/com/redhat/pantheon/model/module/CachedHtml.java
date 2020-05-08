package com.redhat.pantheon.model.module;

import javax.inject.Named;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.WorkspaceChild;

/**
 * Models a single instance of a module's cachedHtml content.
 */
// This node should not be in use after the JCR refactoring and should be deleted
@Deprecated
public interface CachedHtml extends WorkspaceChild {

    @Named("pant:hash")
    Field<String> hash();

    @Named("content")
    Field<FileResource> content();
}