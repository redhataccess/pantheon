package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

/**
 * Simple resource type for an ordered folder. Ordered folders may contain models
 * of several types, all of which may be nested in deep hierarchies.
 * @author Carlos Munoz
 */
@JcrPrimaryType("sling:OrderedFolder")
public interface OrderedFolder extends Folder {
}
