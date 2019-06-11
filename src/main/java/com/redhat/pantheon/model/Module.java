package com.redhat.pantheon.model;

import com.redhat.pantheon.model.api.SlingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;

import javax.annotation.Nonnull;
import java.util.Calendar;

/**
 * Model class to represent modules
 */
public class Module extends SlingResource {

    @Default(values = "pantheon/modules")
    public final Field<String> slingResourceType = new Field<>(String.class, "sling:resourceType");

    public final Field<Calendar> created = new Field<>(Calendar.class, "jcr:created");

    public final Field<String> createdBy = new Field<>(String.class, "jcr:createdBy");

    public final Field<String> primaryType = new Field<>(String.class, "jcr:primaryType");

    public final DeepField<String> asciidocContent = new DeepField<>(String.class, "asciidoc/jcr:content/jcr:data");

    public final DeepField<String> cachedHtmlContent = new DeepField<>(String.class, "cachedContent/jcr:data");

    public final ChildResource<CachedContent> cachedContent = new ChildResource<>(CachedContent.class, "cachedContent");

    public Module(@Nonnull Resource resource) {
        super(resource);
    }

    /**
     * Model class to represent a Module's cached html content.
     * It is a nested class under Module as it only makes sense
     * for these nodes to be generated under a Module.
     */
    public static class CachedContent extends SlingResource {

        public final Field<String> hash = new Field<>(String.class, "pant:hash");

        public final Field<String> data = new Field<>(String.class, "jcr:data");

        public CachedContent(@Nonnull Resource resource) {
            super(resource);
        }
    }
}
