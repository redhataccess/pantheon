package com.redhat.pantheon.model.module;

import javax.inject.Named;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.WorkspaceChild;

/**
 * Models a single instance of a module's cachedHtml content.
 */
public interface CachedHtml extends WorkspaceChild {

    @Named("pant:hash")
    Field<String> hash();

    @Named("jcr:data")
    Field<String> data();
}