package com.redhat.pantheon.model.document;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.workspace.WorkspaceChild;
import org.apache.jackrabbit.JcrConstants;

import javax.inject.Named;
import java.util.Calendar;


public interface Validation extends WorkspaceChild {

    @Named("pant:message")
    Field<String> message();

    @Named("pant:validator")
    Field<String> validator();

    @Named("pant:validationCategory")
    Field<String> validationCategory();

    @Named("jcr:lastModifiedBy")
    Field<String> lastModifiedBy();

    @Named(JcrConstants.JCR_LASTMODIFIED)
    Field<Calendar> dateModified();
}
