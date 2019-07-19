package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.ModifiableValueMap;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A strongly typed jcr field definition for a {@link SlingResource}.
 * Field definitions have a reference to their owning object so they
 * can read and modify said owner when necessary.
 *
 * @param <T>
 * @author Carlos Munoz
 */
public class Field<T> implements Supplier<T>, Consumer<T> {

    private final String name;
    private final Class<T> type;
    private final SlingResource owner;

    Field(String name, Class<T> type, SlingResource owner) {
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
     * @return The field's value from the underlying resource.
     */
    @Override
    public T get() {
        return owner.getValueMap().get(name, type);
    }

    /**
     * Sets the value on the jcr field of the underlying resource
     * @param value
     */
    public void set(T value) {
        owner.adaptTo(ModifiableValueMap.class).put(name, value);
    }

    /**
     * Same as {@link #set(Object)}, just to conform to the {@link Consumer} interface.
     * @see #set(Object)
     * @param t
     */
    @Override
    public void accept(T t) {
        set(t);
    }
}
