package com.redhat.pantheon.model.api;

import org.apache.jackrabbit.JcrConstants;

import javax.inject.Named;
import java.util.Calendar;

/**
 * Adds fields from the mix:created JCR type
 * @author Carlos Munoz
 */
public interface MixCreated {

    @Named(JcrConstants.JCR_CREATED)
    Field<Calendar> created();

    @Named("jcr:createdBy")
    Field<String> createdBy();
}
