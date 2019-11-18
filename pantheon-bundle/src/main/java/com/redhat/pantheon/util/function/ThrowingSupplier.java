package com.redhat.pantheon.util.function;

/**
 * A functional interface to represent a supplier that throws a checked exception.
 * @param <T>
 * @param <E>
 * @author Carlos Munoz
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    T get() throws E;
}
