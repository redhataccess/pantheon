package com.redhat.pantheon.conf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class LoadableValueTest {

    @Mock
    Supplier<String> value1;

    @BeforeEach
    void setCommonMockExpectations() {
        when(value1.get()).thenReturn("Value 1");
    }

    @Test
    void testload() {
        // Given
        LoadableValue<String> loadableValue = new LoadableValue<>(value1);

        // When
        loadableValue.load();

        // Then
        verify(value1, times(1)).get();
        assertEquals("Value 1", loadableValue.get());
    }

    @Test
    void testGetWithoutLoading() {
        // Given
        LoadableValue<String> loadableValue = new LoadableValue<>(value1);

        // When
        loadableValue.get();

        // Then
        assertEquals("Value 1", loadableValue.get());
    }
}