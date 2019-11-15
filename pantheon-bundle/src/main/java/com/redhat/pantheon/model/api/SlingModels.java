package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.lang.reflect.Proxy;

/**
 * Offers utilities to deal with {@link SlingModel} mappings and lifecycle.
 *
 * @author Carlos Munoz
 */
public class SlingModels {

    private SlingModels() {
    }

    /**
     * Returns a {@link SlingModel} instance by looking up a resource in the repository
     * and mapping it to the desired class.
     * @param resourceResolver The resource resolver to fetch resources
     * @param path The absolute path for the resource
     * @param modelType The model type to map it to
     * @param <T> The type of {@link SlingModel} to return
     * @return An instance of T backed by the resource found at path
     */
    public static final <T extends SlingModel>
    T getModel(ResourceResolver resourceResolver, String path, Class<T> modelType) {
        Resource resource = resourceResolver.getResource(path);
        if(resource == null) {
            return null;
        }
        return getModel(resource, modelType);
    }

    /**
     * Returns a {@link SlingModel} instance from the provided {@link Resource}
     * @param resource The resource to convert to a {@link SlingModel}
     * @param modelType The model type to convert the resource
     * @param <T> The type of {@link SlingModel} to return
     * @return An instance of T backed by the provided resource
     */
    public static final <T extends SlingModel>
    T getModel(Resource resource, Class<T> modelType) {
        return (T) Proxy.newProxyInstance(SlingModels.class.getClassLoader(),
                new Class[]{modelType, SlingModel.class},
                new SlingResourceProxy(resource));
    }

}
