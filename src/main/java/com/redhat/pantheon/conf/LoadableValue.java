package com.redhat.pantheon.conf;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * A value which may be loaded at any given time. Once loaded, the value will
 * be cached until the load function is called again.
 * @param <T>
 *
 * @author Carlos Munoz
 */
class LoadableValue<T> implements Supplier<T> {

    private final Supplier<T> loaderFunc;

    private Optional<T> value = empty();

    LoadableValue(Supplier<T> loaderFunc) {
        this.loaderFunc = loaderFunc;
    }

    /**
     * Loads the value by invoking the loader function.
     * The loader function must not return null or else this method will throw
     * a NullPointerException. Consider using an {@link Optional} if the loader
     * function may return a null value.
     */
    public void load() {
        value = of(loaderFunc.get());
    }

    /**
     * Returns the value. If the value hasn't been loaded, it will
     * call the loader function before.
     * @return The stored value
     */
    @Override
    public T get() {
        if (!value.isPresent()) {
            load();
        }
        return value.get();
    }
}
