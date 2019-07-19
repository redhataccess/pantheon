package com.redhat.pantheon.model;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingResource;
import org.apache.sling.api.resource.Resource;

import java.util.Calendar;

/**
 * A {@link SlingResource} which describes the structure for a module revision.
 * Contains all the properties and content for the state of a given module at
 * a given time. ModuleRevisions should differ in content when part of the same
 * parent, but this is not validated.
 */
public class ModuleRevision extends SlingResource {

    public final Child<CachedContent> cachedContent = child("cachedContent", CachedContent.class);

    public final Child<FileResource> asciidoc = file("asciidoc");

    public final Field<Calendar> created = dateField("jcr:created");

    public final Field<String> createdBy = stringField("jcr:createdBy");

    public final Field<String> primaryType = stringField("jcr:primaryType");

    public final Field<String> asciidocContent = stringField("asciidoc/jcr:content/jcr:data");

    public final Field<String> cachedHtmlContent = stringField("cachedContent/jcr:data");

    public ModuleRevision(Resource wrapped) {
        super(wrapped);
    }

    /**
     * A child resource for a {@link ModuleRevision} which contains cached data
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
