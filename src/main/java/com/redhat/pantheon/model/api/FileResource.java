package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingResource;
import org.apache.sling.api.resource.Resource;

public class FileResource extends SlingResource {

    public final Child<JcrContent> jcrContent = child("jcr:content", JcrContent.class, "nt:resource");

    public FileResource(Resource wrapped) {
        super(wrapped);
    }

    public static class JcrContent extends SlingResource {

        public final Field<String> mimeType = stringField("jcr:mimeType");

        public final Field<String> jcrData = stringField("jcr:data");

        public JcrContent(Resource wrapped) {
            super(wrapped);
        }
    }
}
