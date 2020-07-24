package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.document.DocumentMetadata;

import javax.inject.Named;

/**
 * Models an instance of metadata for a assembly. Multiple metadata
 * instances may found on a given assembly representing different
 * versions of said metadata.
 */
public interface AssemblyMetadata extends DocumentMetadata {
}
