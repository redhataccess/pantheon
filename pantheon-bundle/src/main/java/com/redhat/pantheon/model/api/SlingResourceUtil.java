package com.redhat.pantheon.model.api;

import com.google.common.collect.ImmutableMap;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;

/**
 * Utility functions to handle {@link SlingResource} objects.
 */
public final class SlingResourceUtil {

    private SlingResourceUtil() {
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
    public static <T extends SlingModel> T
    createNewSlingResource(Resource parent, String childName, Class<T> modelType) {
        if (parent.getChild(childName) != null) {
            throw new RuntimeException("Tried to create a new resource in an existing path: " + parent.getPath() + "/"
                    + childName);
        }

        try {
            // initial properties
            Map<String, Object> initialProps = new ImmutableMap.Builder<String, Object>()
                    .put(JCR_PRIMARYTYPE, getJcrPrimaryType(modelType))
                    .build();
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
     * @param resourceResolver The resource resolver used to create the child
     * @param path The full absolute path where to create the resource
     * @param modelType The specific type of {@link SlingResource} to return
     * @param <T>
     * @return The newly created resource
     * @throws RuntimeException if the resource already exists at the path
     */
    public static <T extends SlingModel>
    T createNewSlingResource(ResourceResolver resourceResolver, String path, Class<T> modelType) {
        String parentPath = ResourceUtil.getParent(path);
        String resourceName = ResourceUtil.getName(path);
        return createNewSlingResource(resourceResolver.resolve(parentPath),  resourceName, modelType);
    }

    /**
     * Converts a {@link Resource} into a {@link SlingResource}
     *
     * @param backingResource The resource to convert
     * @param modelType       The type of {@link SlingResource} to convert to.
     * @param <T>
     * @return A new object of class modelType wrapping the backingResource, or null if backingResource is null
     */
    public static <T extends SlingModel> T toSlingResource(Resource backingResource, Class<T> modelType) {
        if (backingResource == null) {
            return null;
        }

        try {
            // If the class is an interface, it means a model (v2) is being built, delegate to that class
            if(modelType.isInterface()) {
                return SlingModels.getModel(backingResource, modelType);
            }

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

    /**
     * Renames a resource. This method only changes the name of the resource within its parent, it does not change
     * the parent itself.
     * @param target The resource to rename
     * @param newName The new name for the resource
     * @throws PersistenceException If there is a problem renaming the resource (e.g. another resource with that
     * name already exists)
     */
    public static void rename(final Resource target, final String newName) throws PersistenceException {
        Session jcrSession = target.getResourceResolver().adaptTo(Session.class);
        String currentPath = target.getPath();
        String newPath = target.getParent().getPath() + "/" + newName;
        try {
            jcrSession.move(currentPath, newPath);
        } catch (RepositoryException e) {
            throw new PersistenceException(e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns the primary type for a given {@link SlingResource} class.
     * @param resourceType The resource type
     * @return A String containing the jcr:primaryType to use for the given resource type.
     */
    @Nonnull
    private static final String getJcrPrimaryType(Class<? extends SlingModel> resourceType) {
        JcrPrimaryType primaryType = resourceType.getAnnotation(JcrPrimaryType.class);
        if(primaryType != null) {
            return primaryType.value();
        }
        return SlingResource.DEFAULT_PRIMARY_TYPE;
    }
}
