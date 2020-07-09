package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.Reference;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.document.DocumentMetadata;
import org.apache.jackrabbit.JcrConstants;

import javax.inject.Named;
import java.util.Calendar;

/**
 * Models an instance of metadata for a module. Multiple metadata
 * instances may found on a given module representing different
 * versions of said metadata.
 */
public interface ModuleMetadata extends DocumentMetadata {

    @Named("pant:moduleType")
    Field<ModuleType> moduleType();
}
