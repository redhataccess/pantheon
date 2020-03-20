package com.redhat.pantheon.model.workspace;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.inject.Named;

/**
 * Models an instance of metadata for a module. Multiple metadata
 * instances may found on a given module representing different
 * versions of said metadata.
 */
@JcrPrimaryType("pant:workspace")
public interface Workspace extends SlingModel {

    @Named("pant:attributeFile")
    Field<String> attributeFile();
}