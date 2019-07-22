package com.redhat.pantheon.model;

import java.util.Calendar;

import javax.annotation.Nonnull;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;

import com.redhat.pantheon.model.api.SlingResource;

/**
 * Represents a product as a sling resource.
 * A product has many versions.
 */
public class Product extends SlingResource {

	/**
	 * Represents the product's slingResourceType.
	 */
    @Default(values = "pantheon/product")
    public final Field<String> slingResourceType = new Field<>(String.class, "sling:resourceType");

    /**
     * Represents the product's created date.
     */
    public final Field<Calendar> created = new Field<>(Calendar.class, "jcr:created");

    /**
     * Represents the user who created the product.
     */
    public final Field<String> createdBy = new Field<>(String.class, "jcr:createdBy");

    /**
     * Represents the product's primaryType.
     */
    public final Field<String> primaryType = new Field<>(String.class, "jcr:primaryType");

    /**
     * Represents the product's name.
     */
    public final Field<String> name = new Field<>(String.class, "name");
    
    /**
     * Represents the product's description.
     */
    public final Field<String> description = new Field<>(String.class, "description");
    
    /**
     * Creates a new Product with the given resource object.
     * @param resource
     */
    public Product(@Nonnull Resource resource) {
        super(resource);
    }

}
