package com.redhat.pantheon.model.api;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A strongly typed jcr field definition for a {@link SlingModel}.
 * Field definitions have a reference to their owning object so they
 * can read and modify said owner when necessary.
 *
 * @param <T>
 * @author Carlos Munoz
 */
public interface Field<T> extends Supplier<T>, Consumer<T> {
    String getName();

    Class<T> getType();

    /**
     * Sets the value on the jcr field of the underlying resource.
     * Setting a field to null effectively removes the field from the resource.
     * @param value
     */
    void set(@Nullable T value);

    /**
     * Same as {@link #set(Object)}, just to conform to the {@link Consumer} interface.
     * @see #set(Object)
     * @param t
     */
    @Override
    default void accept(T t) {
        set(t);
    }
}
