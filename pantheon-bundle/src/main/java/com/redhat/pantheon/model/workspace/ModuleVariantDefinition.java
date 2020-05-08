package com.redhat.pantheon.model.workspace;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingModel;

import javax.inject.Named;

/**
 * @author Carlos Munoz
 */
public interface ModuleVariantDefinition extends SlingModel {

    @Named("pant:name")
    Field<String> name();

    @Named("pant:attributesFilePath")
    Field<String> attributesFilePath();

    @Named("pant:canonical")
    Field<Boolean> isCanonical();

}
