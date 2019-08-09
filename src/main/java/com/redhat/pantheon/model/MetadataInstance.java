package com.redhat.pantheon.model;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingResource;
import org.apache.sling.api.resource.Resource;

import java.util.Calendar;

/**
 * Models an instance of metadata for a module. Multiple metadata
 * instances may found on a given module representing different
 * revisions of said metadata.
 */
public class MetadataInstance extends SlingResource {

    public final Field<String> title = stringField("jcr:title");

    public final Field<String> mAbstract = stringField("pant:abstract");

    public final Field<String> description = stringField("jcr:description");

    public final Field<Calendar> created = dateField("jcr:created");

    public final Field<String> createdBy = stringField("jcr:createdBy");

    public final Field<String> primaryType = stringField("jcr:primaryType");

    public MetadataInstance(Resource wrapped) {
        super(wrapped);
    }

}
