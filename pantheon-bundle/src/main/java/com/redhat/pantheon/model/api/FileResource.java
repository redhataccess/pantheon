package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.inject.Named;

/**
 * A concerete implementation of a resource containing a file which
 * adheres to the node structure expected by Sling and JCR.
 */
@JcrPrimaryType("nt:file")
public interface FileResource extends SlingModel {

    @Named("jcr:content")
    Child<JcrContent> jcrContent();

    @JcrPrimaryType("nt:resource")
    interface JcrContent extends SlingModel {

        @Named("jcr:mimeType")
        Field<String> mimeType();

        @Named("jcr:data")
        Field<String> jcrData();
    }
}
