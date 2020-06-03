package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.annotation.JcrMixins;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.inject.Named;

/**
 * Extension of the {@link FileResource} which adds a hash field.
 * @author Carlos Munoz
 */
@JcrPrimaryType("nt:file")
@JcrMixins("pant:hashable")
public interface HashableFileResource extends FileResource {

    @Named("pant:hash")
    Field<String> hash();
}
