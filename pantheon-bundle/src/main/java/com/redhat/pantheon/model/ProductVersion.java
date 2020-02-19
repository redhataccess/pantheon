package com.redhat.pantheon.model;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingModel;
import org.apache.sling.models.annotations.Default;

import javax.inject.Named;
import java.util.Calendar;
import com.redhat.pantheon.model.module.ModuleLocale;

/**
 * Represents a ProductVersion as a sling resource.
 * A productVersion is associated with a product.
 */
public interface ProductVersion extends SlingModel {

	/**
	 * Represents the productVersion's slingResourceType.
	 */
    @Default(values = "pantheon/productVersion")
    @Named("sling:resourceType")
    Field<String> slingResourceType();

    /**
     * Represents the productVersion's created date.
     */
    @Named("jcr:created")
    Field<Calendar> created();

    /**
     * Represents the user who created the productVersion.
     */
    @Named("jcr:createdBy")
    Field<String> createdBy();

    /**
     * Represents the productVersion's primaryType.
     */
    @Named("jcr:primaryType")
    Field<String> primaryType();

    /**
     * Represents the productVersion's name.
     */
    Field<String> name();

    /**
     * Get Product object from Version.
     * @return Product
     */
    default Product getProduct() {
        return getParent().getParent().adaptTo(Product.class);
    }
}
