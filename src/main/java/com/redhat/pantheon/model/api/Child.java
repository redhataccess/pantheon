package com.redhat.pantheon.model.api;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.redhat.pantheon.model.api.SlingResourceUtil.createNewSlingResource;
import static com.redhat.pantheon.model.api.SlingResourceUtil.toSlingResource;

/**
 * A strongly typed child resource definition for a {@link SlingResource}.
 * Child definitions have a reference to their owning object so they
 * can read and modify said owner when necessary.
 *
 * @param <T>
 * @author Carlos Munoz
 */
public class Child<T extends SlingResource> implements Supplier<T> {

    private final String name;
    private final Class<T> type;
    private final SlingResource owner;

    Child(String name, Class<T> type, SlingResource owner) {
        this.name = name;
        this.type = type;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * Returns the child resource by the definition's name, cast to the
     * type indicated in this definition.
     */
    @Override
    public T get() {
        return toSlingResource(owner.getChild(name), type);
    }

    /**
     * Returns the child as indicated by the definition's name, creating it
     * in the process if necessary.
     * @return The child resource as indicated by this definition
     */
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
    public T create() {
        return createNewSlingResource(owner, name, type);
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
     */
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
