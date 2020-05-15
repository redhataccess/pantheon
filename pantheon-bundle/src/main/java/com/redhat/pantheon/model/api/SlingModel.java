package com.redhat.pantheon.model.api;

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
     * Returns a {@link Child} of this resource.
     * @param name The name of the child
     * @param type The model type for this child
     * @param <T>
     * @return The child object
     */
    <T extends SlingModel> Child<T> child(String name, Class<T> type);

    /**
     * Returns a resource's {@link Field}
     * @param name The name of the field
     * @param type The type of the field
     * @param <T>
     * @return The {@link Field} object.
     */
    <T> Field<T> field(String name, Class<T> type);

    /**
     * Deletes this resource from the repository.
     * @throws PersistenceException If there was a problem deleting the node, such as a failed constraing validation
     */
    void delete() throws PersistenceException;
}
