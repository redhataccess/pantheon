package com.redhat.pantheon.model.api;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Utility functions to handle {@link SlingResource} objects.
 */
public final class SlingResourceUtil {

    private SlingResourceUtil() {
    }

    public static <T extends SlingResource> T
    createNewSlingResource(Resource parent, String childName, Map<String, Object> initialProps, Class<T> modelType) {
        if (parent.getChild(childName) != null) {
            throw new RuntimeException("Tried to create a new resource in an existing path: " + parent.getPath() + "/"
                    + childName);
        }

        try {
            // create the resource
            Resource childResource = parent.getResourceResolver().
                    create(parent, childName, initialProps);
            // create the model
            T model = toSlingResource(childResource, modelType);
            return model;
        } catch (PersistenceException e) {
            throw new RuntimeException("Exception while creating new resource: " + parent.getPath() + "/"
                    + childName, e);
        }
    }

    /**
     * Creates a new {@link SlingResource} of a given type.
     *
     * @param parent    The parent resource (where the new resource will reside)
     * @param childName The name of the child resource to create.
     * @param modelType The specific class of {@link SlingResource} to return.
     * @param <T>
     * @return A newly created resource wrapped around the provided model type.
     * @throws RuntimeException if the resource already exists.
     */
    public static <T extends SlingResource> T
    createNewSlingResource(Resource parent, String childName, Class<T> modelType) {
        return createNewSlingResource(parent, childName, newHashMap(), modelType );
    }

    /**
     * Converts a {@link Resource} into a {@link SlingResource}
     *
     * @param backingResource The resource to convert
     * @param modelType       The type of {@link SlingResource} to convert to.
     * @param <T>
     * @return A new object of class modelType wrapping the backingResource, or null if backingResource is null
     */
    public static <T extends SlingResource> T toSlingResource(Resource backingResource, Class<T> modelType) {
        if (backingResource == null) {
            return null;
        }

        try {
            // If there is a one-arg constructor, invoke it
            Constructor<T> constructor = modelType.getConstructor(Resource.class);

            if (constructor != null) {
                return constructor.newInstance(backingResource);
            } else {
                // Otherwise, throw an exception
                throw new RuntimeException("SlingResource sub classes need an empty constructor with one argument " +
                        "of type " + Resource.class.getName());
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Exception while converting a Resource to a SlingResource", e);
        }
    }

    public static final Pair<String, String> primaryType(String primaryType) {
        return Pair.of("jcr:primaryType", primaryType);
    }
}
