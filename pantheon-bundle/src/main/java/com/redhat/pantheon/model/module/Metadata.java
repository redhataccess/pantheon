package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingResource;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.Resource;

import java.util.Calendar;

/**
 * Models an instance of metadata for a module. Multiple metadata
 * instances may found on a given module representing different
 * versions of said metadata.
 */
public class Metadata extends SlingResource {

    public final Field<String> title = stringField("jcr:title");

    public final Field<String> mAbstract = stringField("pant:abstract");

    public final Field<String> headline = stringField("pant:headline");

    public final Field<String> description = stringField("jcr:description");

    public final Field<Calendar> created = dateField("jcr:created");

    public final Field<String> createdBy = stringField("jcr:createdBy");

    public final Field<ModuleType> moduleType = enumField("pant:moduleType", ModuleType.class);

    public final Field<Calendar> datePublished = dateField("pant:datePublished");

    public final Field<Calendar> dateUploaded = dateField("pant:dateUploaded");

    public final Field<Calendar> dateModified = dateField(JcrConstants.JCR_LASTMODIFIED);

    public final Field<String> primaryType = stringField("jcr:primaryType");

    public Metadata(Resource wrapped) {
        super(wrapped);
    }

}
