package com.redhat.pantheon.model.api;

import com.google.common.collect.ImmutableMap;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.lang.reflect.Proxy;
import java.util.Map;

import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;

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
        if(resource == null) {
            return null;
        }
        return (T) Proxy.newProxyInstance(SlingModels.class.getClassLoader(),
                new Class[]{modelType, SlingModel.class},
                new SlingResourceProxy(resource));
    }

    /**
     * Creates a new {@link SlingModel} of a given type.
     *
     * @param parent    The parent resource (where the new resource will reside)
     * @param childName The name of the child resource to create.
     * @param modelType The specific class of {@link SlingModel} to return.
     * @param <T>
     * @return A newly created resource wrapped around the provided model type.
     * @throws RuntimeException if the resource already exists.
     */
    public static <T extends SlingModel> T
    createModel(Resource parent, String childName, Class<T> modelType) {
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
            T model = getModel(childResource, modelType);
            return model;
        } catch (PersistenceException e) {
            throw new RuntimeException("Exception while creating new resource: " + parent.getPath() + "/"
                    + childName, e);
        }
    }

    /**
     * Creates a new {@link SlingModel} of a given type.
     *
     * @param resourceResolver The resource resolver used to create the child
     * @param path The full absolute path where to create the resource
     * @param modelType The specific type of {@link SlingModel} to return
     * @param <T>
     * @return The newly created resource
     * @throws RuntimeException if the resource already exists at the path
     */
    public static <T extends SlingModel>
    T createModel(ResourceResolver resourceResolver, String path, Class<T> modelType) {
        String parentPath = ResourceUtil.getParent(path);
        String resourceName = ResourceUtil.getName(path);
        return createModel(resourceResolver.resolve(parentPath),  resourceName, modelType);
    }

    /**
     * Renames the resource represented by the given model. This method invokes a move operation
     * in the jcr session, it will only work against JCR backed resources.
     * @param model The model object wrapping the resource to rename
     * @param newName The new name for the resource.
     * @param modelType The type of model to be returned.
     * @throws RepositoryException If there is a problem renaming the model
     */
    public static <T extends SlingModel>
    T rename(@Nonnull final T model, final String newName, final Class<T> modelType) throws RepositoryException {
        String newAbsPath = PathUtils.concat(model.getParent().getPath(), newName);
        ResourceResolver resourceResolver = model.getResourceResolver();
        resourceResolver.adaptTo(Session.class)
                .move( model.getPath(), newAbsPath );
        return getModel(resourceResolver.getResource(newAbsPath), modelType);
    }

    /**
     * Returns the primary type for a given {@link SlingModel} class.
     * @param resourceType The resource type
     * @return A String containing the jcr:primaryType to use for the given resource type.
     */
    @Nonnull
    private static final String getJcrPrimaryType(Class<? extends SlingModel> resourceType) {
        JcrPrimaryType primaryType = resourceType.getAnnotation(JcrPrimaryType.class);
        if(primaryType != null) {
            return primaryType.value();
        }
        return ResourceDecorator.DEFAULT_PRIMARY_TYPE;
    }
}
