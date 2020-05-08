package com.redhat.pantheon.model.api;

import com.google.common.collect.Streams;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import java.util.stream.Stream;

/**
 * Represents a regular Sling folder. folders may contain models of several types,
 * all of which may be nested in deep hierarchies. If used directly, this will create
 * child nodes with primary type of sling:Folder.
 * @author Carlos Munoz
 */
@JcrPrimaryType("sling:Folder")
public interface Folder extends SlingModel {

    /**
     * Converts this folder to a stream of {@link SlingModel}s. This method is useful
     * when the folder is sure to contain nodes which are mappable to the same model
     * type.
     * @param modelType The model type contained in the folder
     * @param <T>
     * @return This folder as a stream of its child resources, converted to a specific
     * model type
     */
    default <T extends SlingModel> Stream<T> as(Class<T> modelType) {
        return Streams.stream(getChildren())
                .map(resource -> SlingModels.getModel(resource, modelType));
    }
}
