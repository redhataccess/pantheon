package com.redhat.pantheon.model.api;

import java.util.Optional;
import java.util.function.Function;

import static com.redhat.pantheon.model.api.SlingModels.getModel;

/**
 * Default implementation of the {@link Child} interface.
 * A strongly typed child resource definition for a {@link SlingModel}.
 * Child definitions have a reference to their owning parent so they
 * can read and modify said owner when necessary.
 *
 * @author Carlos Munoz
 */
public class ChildImpl<T extends SlingModel> implements Child<T> {

    private final String name;
    private final Class<T> type;
    private final SlingModel owner;

    // TODO Make this package protected
    public ChildImpl(String name, Class<T> type, SlingModel owner) {
        this.name = name;
        this.type = type;
        this.owner = owner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns the child resource by the definition's name, cast to the
     * type indicated in this definition.
     */
    @Override
    public T get() {
        return getModel(owner.getChild(name), type);
    }

    /**
     * Returns the child as indicated by the definition's name, creating it
     * in the process if necessary.
     * @return The child resource as indicated by this definition
     */
    @Override
    public T getOrCreate() {
        if(owner.getChild(name) == null) {
            return create();
        }
        return get();
    }

    /**
     * Attempts to create the child as indicated by this definition. This might
     * throw an exception if the child already exists.
     * @return The newly created child resource
     */
    @Override
    public T create() {
        return SlingModels.createModel(owner, name, type);
    }

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
    @Override
    public <R> Optional<R> map(Function<? super T, ? extends R> func) {
        T value = get();
        if(value == null) {
            return Optional.empty();
        }
        else {
            return Optional.ofNullable(func.apply(value));
        }
    }
}
