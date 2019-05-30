package com.redhat.pantheon.model;

import org.apache.sling.api.resource.Resource;

import java.util.Calendar;

public class ContentModule extends JcrModel{

    public final Field<String> JCR_CREATED_BY = new Field<>(String.class, "jcr:createdBy");
    public final Field<Calendar> JCR_CREATED = new Field<>(Calendar.class,"jcr:created");

    public ContentModule(Resource resource) {
        super(resource);
    }
}
