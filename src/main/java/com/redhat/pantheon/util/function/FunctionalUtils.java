package com.redhat.pantheon.util.function;

import java.util.function.Supplier;

/**
 * Utilities for functional programming and handling of functional interfaces.
 *
 * @author Carlos Munoz
 */
public class FunctionalUtils {

    private FunctionalUtils(){
        // no-arg private constructor as this is a function's class
    }

    /**
     * Wraps supplier functions that throw an exception around a new {@link Supplier}
     * which wraps any exception in a {@link RuntimeException}
     * @param supplier The supplier which throws an Exception.
     * @param <T>
     * @return A new RuntimeException-throwing {@link Supplier}. Any checked exceptions thrown by supplier
     * will be instead wrapped around a RuntimeException
     */
    public static <T> Supplier<T> tryAndThrowRuntime(final ThrowingSupplier<T, ?> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
