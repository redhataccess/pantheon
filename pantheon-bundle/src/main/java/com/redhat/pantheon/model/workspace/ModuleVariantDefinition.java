package com.redhat.pantheon.model.workspace;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingModel;

import javax.inject.Named;

/**
 * @author Carlos Munoz
 */
public interface ModuleVariantDefinition extends SlingModel {

    // TODO We might not need this, as the identifier will be the node name
    // There might be potential conflicts.... but we could impose rules on the naming schema for variants
    @Deprecated
    @Named("pant:name")
    Field<String> name();

    @Named("pant:attributesFilePath")
    Field<String> attributesFilePath();

    @Named("pant:canonical")
    Field<Boolean> isCanonical();

}
