package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import org.apache.sling.api.resource.Resource;

/**
 * A concerete implementation of a resource containing a file which
 * adheres to the node structure expected by Sling and JCR.
 */
@JcrPrimaryType("nt:file")
public class FileResource extends SlingResource {

    public final Child<JcrContent> jcrContent = child("jcr:content", JcrContent.class);

    public FileResource(Resource wrapped) {
        super(wrapped);
    }

    @JcrPrimaryType("nt:resource")
    public static class JcrContent extends SlingResource {

        public final Field<String> mimeType = stringField("jcr:mimeType");

        public final Field<String> jcrData = stringField("jcr:data");

        public JcrContent(Resource wrapped) {
            super(wrapped);
        }
    }
}
