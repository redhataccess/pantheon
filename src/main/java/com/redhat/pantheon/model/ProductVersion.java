package com.redhat.pantheon.model;

import java.util.Calendar;

import javax.annotation.Nonnull;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingResource;
import com.redhat.pantheon.model.module.ModuleLocale;

/**
 * Represents a ProductVersion as a sling resource.
 * A productVersion is associated with a product.
 */
public class ProductVersion extends SlingResource {

	/**
	 * Represents the productVersion's slingResourceType.
	 */
    @Default(values = "pantheon/productVersion")
    public final Field<String> slingResourceType = stringField("sling:resourceType");

    /**
     * Represents the productVersion's created date.
     */
    public final Field<Calendar> created = dateField("jcr:created");

    /**
     * Represents the user who created the productVersion.
     */
    public final Field<String> createdBy = stringField("jcr:createdBy");

    /**
     * Represents the productVersion's primaryType.
     */
    public final Field<String> primaryType = stringField("jcr:primaryType");

    /**
     * Represents the productVersion's name.
     */
    public final Field<String> name = stringField("name");
    
    /**
     * Creates a new ProductVersion with given resource.
     * @param resource
     */
    public ProductVersion(@Nonnull Resource resource) {
        super(resource);
    }

}
