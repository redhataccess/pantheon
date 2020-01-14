package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.Reference;
import com.redhat.pantheon.model.api.SlingModel;
import org.apache.jackrabbit.JcrConstants;

import javax.inject.Named;
import java.util.Calendar;
import java.util.Date;

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
    Field<Date> datePublished();
    
    @Named("pant:dateUploaded")
    Field<Date> dateUploaded();

    @Named(JcrConstants.JCR_LASTMODIFIED)
    Field<Calendar> dateModified();

    Reference<ProductVersion> productVersion();

    Field<String> urlFragment();

    Field<String> searchKeywords();

    @Named(JcrConstants.JCR_PRIMARYTYPE)
    Field<String> primaryType();
}
