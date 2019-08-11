package com.redhat.pantheon.util.function;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void toLastElement() {
        // Given
        List<String> elements = Lists.asList("A", new String[]{"B", "C", "D", "E"});

        // When
        assertEquals("E", elements.stream().reduce(FunctionalUtils.toLastElement()).get());

    }
}