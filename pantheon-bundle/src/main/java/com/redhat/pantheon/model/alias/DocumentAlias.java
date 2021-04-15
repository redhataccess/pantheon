package com.redhat.pantheon.model.alias;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.Reference;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.document.DocumentVariant;

import javax.inject.Named;

public interface DocumentAlias extends SlingModel {

    Reference<DocumentVariant> target();

    @Named("sling:resourceType")
    Field<String> slingResourceType();
}
