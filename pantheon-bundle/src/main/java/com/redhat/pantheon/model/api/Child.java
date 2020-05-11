package com.redhat.pantheon.model.api;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A strongly typed child resource definition for a {@link SlingModel}.
 * Child definitions have a reference to their owning object so they
 * can read and modify said owner when necessary.
 *
 * @param <T>
 * @author Carlos Munoz
 */
public interface Child<T extends SlingModel> extends Supplier<T> {
    String getName();

    Class<T> getType();

    /**
     * Returns the child as indicated by the definition's name, creating it
     * in the process if necessary.
     * @return The child resource as indicated by this definition
     */
    T getOrCreate();

    /**
     * Attempts to create the child as indicated by this definition. This might
     * throw an exception if the child already exists.
     * @return The newly created child resource
     */
    T create();

    /**
     * Provides a null-safe way to operate on the value of the child, and return an
     * {@link Optional} with the result of the operation. This allowes the caller to
     * continue to operapate in a null-safe fashion.
     * @param func The function to apply to the value
     * @param <R>
     * @return An optional indicating the result of the operation. If the operation
     * returns null, or if the value of this child was not present in the first place,
     * this returns an empty Optional
     * @deprecated Use {@link com.redhat.pantheon.model.api.util.SafeResourceTraversal#start(SlingModel)}
     * for safe resource traversals
     */
    @Deprecated
    <R> Optional<R> map(Function<? super T, ? extends R> func);
}
