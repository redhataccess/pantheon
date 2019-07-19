package com.redhat.pantheon.model;

import java.util.Calendar;

import javax.annotation.Nonnull;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;

import com.redhat.pantheon.model.api.SlingResource;

public class Product extends SlingResource {

    @Default(values = "pantheon/product")
    public final Field<String> slingResourceType = new Field<>(String.class, "sling:resourceType");

    public final Field<Calendar> created = new Field<>(Calendar.class, "jcr:created");

    public final Field<String> createdBy = new Field<>(String.class, "jcr:createdBy");

    public final Field<String> primaryType = new Field<>(String.class, "jcr:primaryType");

    public final Field<String> name = new Field<>(String.class, "name");
    
    public final Field<String> productDescription = new Field<>(String.class, "productDescription");
    
    public Product(@Nonnull Resource resource) {
        super(resource);
    }

}
