package com.redhat.pantheon.util.function;

import javax.annotation.Nullable;
import java.util.function.BinaryOperator;
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

    /**
     * A binary operator which helps reduce a stream to its last element only.
     * Should help to run something like:
     * stream.reduce(toLastElement())
     */
    public static <T> BinaryOperator<T> toLastElement() {
        return (T o1, T o2) -> o2;
    }

    /**
     * Provides a null-safe way to execute code.
     * This is useful when chaining several calls together and ignoring any
     * {@link NullPointerException}s which may happen in between, instead
     * turning those into a final null result. This function effectively
     * replaces all NullPointerExceptions with a single null result
     * @param nonNullSafe the piece of code which may throw a {@link NullPointerException}
     * @param <T>
     * @return The final value, or null of there was a NullPointerException thrown.
     */
    public static @Nullable
    <T> T nullSafe(final Supplier<T> nonNullSafe) {
        try {
            return nonNullSafe.get();
        } catch (NullPointerException npe) {
            return null;
        }
    }
}
