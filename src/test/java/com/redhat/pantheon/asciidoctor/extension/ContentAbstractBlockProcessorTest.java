package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.module.Metadata;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class})
class ContentAbstractBlockProcessorTest {

    SlingContext sc = new SlingContext();

    @Test
    void process() {
        // Given
        sc.build()
                .resource("/metadata")
                .commit();
        String content = "== A title\n" +
                "\n" +
                "\n[pantheon:abstract]" +
                "\nThis is my abstract.";
        Metadata metadata = new Metadata(sc.resourceResolver().getResource("/metadata"));
        ContentAbstractBlockProcessor extension = new ContentAbstractBlockProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().block(extension);

        // When
        asciidoctor.load(content, new HashMap<>());

        // Then
        assertEquals("This is my abstract.", metadata.mAbstract.get());
    }

    @Test
    void noAbstractPresent() {
        // Given
        sc.build()
                .resource("/metadata")
                .commit();
        String content = "== A title\n" +
                "\n" +
                "\nThis is my abstract.";
        Metadata metadata = new Metadata(sc.resourceResolver().getResource("/metadata"));
        ContentAbstractBlockProcessor extension = new ContentAbstractBlockProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().block(extension);

        // When
        asciidoctor.load(content, new HashMap<>());

        // Then
        assertNull(metadata.mAbstract.get());
    }
}