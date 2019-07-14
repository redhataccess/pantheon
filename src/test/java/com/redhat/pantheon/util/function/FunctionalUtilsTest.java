package com.redhat.pantheon.util.function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FunctionalUtilsTest {

    @Test
    void tryAndThrowRuntime() {
        // Given
        ThrowingSupplier<Object, Exception> throwingSupplier = () -> {
            return null;
        };

        // Then
        assertDoesNotThrow(() -> FunctionalUtils.tryAndThrowRuntime(throwingSupplier).get());
    }

    @Test
    void tryAndThrowRuntimeWithException() {
        // Given
        ThrowingSupplier<Object, Exception> throwingSupplier = () -> {
            throw new Exception();
        };

        // Then
        assertThrows(RuntimeException.class, () -> FunctionalUtils.tryAndThrowRuntime(throwingSupplier).get());
    }
}