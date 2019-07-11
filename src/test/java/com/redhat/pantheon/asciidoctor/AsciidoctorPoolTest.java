package com.redhat.pantheon.asciidoctor;

import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import org.apache.sling.api.resource.Resource;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class AsciidoctorPoolTest {

    @Mock(answer = RETURNS_MOCKS)
    AsciidoctorLifecycle lifecycle;

    @Mock(answer = RETURNS_MOCKS)
    Resource resource;

    @Test
    void borrowAsciidoctorObject() {
        // Given
        Asciidoctor asciidoctor = mock(Asciidoctor.class);
        JavaExtensionRegistry extensionReg = mock(JavaExtensionRegistry.class);
        lenient().when(lifecycle.createInstance()).thenReturn(asciidoctor);
        lenient().when(asciidoctor.javaExtensionRegistry()).thenReturn(extensionReg);
        AsciidoctorPool pool = new AsciidoctorPool(lifecycle);

        // When
        Asciidoctor obtainedInstance = pool.borrowAsciidoctorObject(resource);

        // Then
        assertNotNull(obtainedInstance);
        verify(extensionReg, times(1)).includeProcessor(any(SlingResourceIncludeProcessor.class));
    }
}