package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.SlingResource;
import org.apache.sling.api.resource.Resource;

/**
 * Models a single instance of a module's content. Multiple content instances may be found on a
 * given module representing several content versions.
 */
public class Content extends SlingResource {

    public final Field<String> asciidocContent = stringField("asciidoc/jcr:content/jcr:data");

    public final Child<CachedContent> cachedHtml = child("cachedHtml", CachedContent.class);

    public final Child<FileResource> asciidoc = file("asciidoc");

    public Content(Resource wrapped) {
        super(wrapped);
    }

    /**
     * A child resource for a {@link ModuleVersion} which contains cached data
     * when a resource is generated.
     */
    public static class CachedContent extends SlingResource {

        public final Field<String> hash = stringField("pant:hash");

        public final Field<String> data = stringField("jcr:data");

        public CachedContent(Resource wrapped) {
            super(wrapped);
        }
    }
}
