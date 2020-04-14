package com.redhat.pantheon.model.module;

import java.util.Calendar;

import javax.inject.Named;

import org.apache.jackrabbit.JcrConstants;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

@JcrPrimaryType("pant:acknowledgment")
public interface AckStatus extends WorkspaceChild {
    
    @Named("pant:message")
    Field<String> message();
    
    @Named("pant:status")
    Field<String> status();
    
    @Named("pant:sender")
    Field<String> sender();
    
    @Named("jcr:lastModifiedBy")
    Field<String> lastModifiedBy();

    @Named(JcrConstants.JCR_LASTMODIFIED)
    Field<Calendar> dateModified();
}
