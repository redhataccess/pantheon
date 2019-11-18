package com.redhat.pantheon.model.api.v2;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;

/**
 * Interface that all Sling persistent entities must extend. It adds utility
 * methods on top of those already offered by the {@link Resource} class.
 *
 * @author Carlos Munoz
 */
public interface SlingModel extends Resource {

    /**
     * Creates a child resource given a name and a set of initial properties
     * @param name The child's name
     * @param modelType The {@link SlingModel} type to create the child as.
     * @return The newly created child resource
     * @throws PersistenceException If there is a problem creating the child resource
     */
    <T extends SlingModel> T createChild(String name, Class<T> modelType);

    /**
     * Returns a child resource
     * @param name The child's name
     * @param type The type of {@link SlingModel} to interpret it as
     * @param <T>
     * @return The child resource or null if one doesn't exist by the given name
     */
    <T extends SlingModel> T getChild(String name, Class<T> type);

    /**
     * Returns a child resource, creating it if it doesn't exist.
     * @param name The child's name
     * @return The found or created child resource
     * @throws PersistenceException If there is a problem creating the new child resource
     */
    <T extends SlingModel> T getOrCreateChild(String name, Class<T> type);

    /**
     * Returns a resource's property
     * @param name The name of the property
     * @param type The type into which to cast the property value
     * @param <T>
     * @return The property's value, or null if no such property exists
     */
    <T> T getProperty(String name, Class<T> type);

    /**
     * Sets a resource's property. This is a convenience method. Be aware that the underlying
     * implementation might throw an exception if the resource is not modifiable, or if the type
     * of value is not recognized
     * @param name The name of the property
     * @param value The value to set
     */
    void setProperty(String name, Object value);

    /**
     * Deletes this resource from the repository.
     * @throws PersistenceException If there was a problem deleting the node, such as a failed constraing validation
     */
    void delete() throws PersistenceException;
}
