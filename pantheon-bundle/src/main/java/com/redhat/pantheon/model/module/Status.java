package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.WorkspaceChild;

import javax.inject.Named;

/**
 * Models an instance of status acknowledgement sent by integrated systems
 */
public interface Status extends WorkspaceChild {
    @Named("pant:status")
    Field<String> status();
    @Named("pant:message")
    Field<String> message();
    @Named("pant:sender")
    Field<String> sender();
}
