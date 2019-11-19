package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.api.v2.SlingModel;
import com.redhat.pantheon.model.api.ReferenceField;
import org.apache.jackrabbit.JcrConstants;

import javax.inject.Named;
import java.util.Calendar;

/**
 * Models an instance of metadata for a module. Multiple metadata
 * instances may found on a given module representing different
 * versions of said metadata.
 */
public interface Metadata extends SlingModel {

    @Named("jcr:title")
    Field<String> title();

    @Named("pant:abstract")
    Field<String> mAbstract();

    @Named("pant:headline")
    Field<String> headline();

    @Named("jcr:description")
    Field<String> description();

    @Named("jcr:created")
    Field<Calendar> created();

    @Named("jcr:createdBy")
    Field<String> createdBy();

    @Named("pant:moduleType")
    Field<ModuleType> moduleType();

    @Named("pant:datePublished")
    Field<Calendar> datePublished();

    @Named("pant:dateUploaded")
    Field<Calendar> dateUploaded();

    public final ReferenceField<ProductVersion> productVersion = referenceField("productVersion", ProductVersion.class);

    public final Field<String> urlFragment = stringField("urlFragment");


    @Named(JcrConstants.JCR_PRIMARYTYPE)
    Field<String> primaryType();
}
