package com.redhat.pantheon.model.api;

import javax.jcr.RepositoryException;

/**
 * Specific implementation of a field of type reference.
 * Reference fields add convenience to fetch the referenced resource.
 *
 * @author Carlos Munoz
 */
public interface Reference<T extends SlingModel> extends Field<String> {

    /**
     * @return The referenced object as a {@link SlingModel}, or null if the reference is null or
     * invalid
     * @throws RepositoryException If there was a problem getting the referenced value
     */
    T getReference() throws RepositoryException;
}
