package com.redhat.pantheon.model;

import java.util.Calendar;

import javax.annotation.Nonnull;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;

import com.redhat.pantheon.model.api.SlingResource;

/**
 * Represents a ProductVersion as a sling resource.
 * A productVersion is associated with a product.
 */
public class ProductVersion extends SlingResource {

	/**
	 * Represents the productVersion's slingResourceType.
	 */
    @Default(values = "pantheon/productVersion")
    public final Field<String> slingResourceType = new Field<>(String.class, "sling:resourceType");

    /**
     * Represents the productVersion's created date.
     */
    public final Field<Calendar> created = new Field<>(Calendar.class, "jcr:created");

    /**
     * Represents the user who created the productVersion.
     */
    public final Field<String> createdBy = new Field<>(String.class, "jcr:createdBy");

    /**
     * Represents the productVersion's primaryType.
     */
    public final Field<String> primaryType = new Field<>(String.class, "jcr:primaryType");

    /**
     * Represents the productVersion's name.
     */
    public final Field<String> name = new Field<>(String.class, "name");
    
    /**
     * Creates a new ProductVersion with given resource.
     * @param resource
     */
    public ProductVersion(@Nonnull Resource resource) {
        super(resource);
    }

}
