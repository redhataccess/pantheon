package com.redhat.pantheon.model.document;

import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.Reference;
import com.redhat.pantheon.model.workspace.WorkspaceChild;
import org.apache.jackrabbit.JcrConstants;

import javax.inject.Named;
import java.util.Calendar;

/**
 * Models an instance of metadata for a module. Multiple metadata
 * instances may found on a given module representing different
 * versions of said metadata.
 */
public interface DocumentMetadata extends WorkspaceChild {

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

    /**
     * This represents the date that a document was published *most recently* and is not carried forward from version
     * to version (since a document version should record its own publish date). Contract this with 'dateFirstPublished'
     * which deals with the document's *first ever* publish date.
     * @return
     */
    @Named("pant:datePublished")
    Field<Calendar> datePublished();

    /**
     * This represents the date that a document was published *for the first time ever* and is carried forward from
     * version to version. Contrast this with 'datePublished' which deals with the *most recent* publish date.
     * @return
     */
    @Named("pant:dateFirstPublished")
    Field<Calendar> dateFirstPublished();

    @Deprecated
    @Named("pant:dateUploaded")
    Field<Calendar> dateUploaded();

    @Named(JcrConstants.JCR_LASTMODIFIED)
    Field<Calendar> dateModified();

    Reference<ProductVersion> productVersion();

    Field<String> urlFragment();

    Field<String> searchKeywords();

    @Named(JcrConstants.JCR_PRIMARYTYPE)
    Field<String> primaryType();
}
