package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static java.util.Collections.emptyMap;

/**
 * Utility functions to handle {@link SlingResource} objects.
 */
final class SlingResourceUtil {

    private SlingResourceUtil() {
    }

    /**
     * Creates a new {@link SlingResource} of a given type.
     * @param parent The parent resource (where the new resource will reside)
     * @param childName The name of the child resource to create.
     * @param modelType The specific class of {@link SlingResource} to return.
     * @param <T>
     * @return A newly created resource wrapped around the provided model type.
     * @throws RuntimeException if the resource already exists.
     */
    public static <T extends SlingResource> T
    createNewSlingResource(Resource parent, String childName, Class<T> modelType) {
        if (parent.getChild(childName) != null) {
            throw new RuntimeException("Tried to create a new resource in an existing path: " + parent.getPath() + "/"
                    + childName);
        }

        try {
            // create the resource
            Resource childResource = parent.getResourceResolver().
                    create(parent, childName, emptyMap());
            // create the model
            T model = toSlingResource(childResource, modelType);
            return model;
        } catch (PersistenceException e) {
            throw new RuntimeException("Exception while creating new resource: " + parent.getPath() + "/"
                    + childName, e);
        }
    }

    /**
     * Converts a {@link Resource} into a {@link SlingResource}
     * @param backingResource The resource to convert
     * @param modelType The type of {@link SlingResource} to convert to.
     * @param <T>
     * @return A new object of class modelType wrapping the backingResource
     */
    public static <T extends SlingResource> T toSlingResource(Resource backingResource, Class<T> modelType) {
        // If there is a one-arg constructor, invoke it
        try {
            Constructor<T> constructor = modelType.getConstructor(Resource.class);

            if (constructor != null) {
                return constructor.newInstance(backingResource);
            }

            // Otherwise, just invoke the empty constructor and reflectively set the 'resource' property
            constructor = modelType.getConstructor();

            if (constructor == null) {
                throw new RuntimeException("The class " + modelType.getName() + " need one of the following constructors: " +
                        "SlingResource(Resource) or SlingResource()");
            }

            T newInstance = constructor.newInstance();
            Field resourceField = SlingResource.class.getDeclaredField("resource");
            resourceField.setAccessible(true);
            resourceField.set(newInstance, backingResource);

            return newInstance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchFieldException e) {
            throw new RuntimeException("Exception while creating a SlingResource of type: " + modelType.getName(), e);
        }
    }
}
