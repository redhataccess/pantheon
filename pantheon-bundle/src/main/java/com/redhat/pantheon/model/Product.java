package com.redhat.pantheon.model;

import com.redhat.pantheon.model.api.v2.Field;
import com.redhat.pantheon.model.api.v2.SlingModel;
import org.apache.sling.models.annotations.Default;

import javax.inject.Named;
import java.util.Calendar;

/**
 * Represents a product as a sling resource.
 * A product has many versions.
 */
public interface Product extends SlingModel {

	/**
	 * Represents the product's slingResourceType.
	 */
    @Default(values = "pantheon/product")
    @Named("sling:resourceType")
    Field<String> slingResourceType();

    /**
     * Represents the product's created date.
     */
    @Named("jcr:created")
    Field<Calendar> created();

    /**
     * Represents the user who created the product.
     */
    @Named("jcr:createdBy")
    Field<String> createdBy();

    /**
     * Represents the product's primaryType.
     */
    @Named("jcr:primaryType")
    Field<String> primaryType();

    /**
     * Represents the product's name.
     */
    Field<String> name();
    
    /**
     * Represents the product's description.
     */
    Field<String> description();
}
