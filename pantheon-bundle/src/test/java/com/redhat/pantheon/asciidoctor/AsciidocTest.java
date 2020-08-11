package com.redhat.pantheon.asciidoctor;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * This test class should be used to contain any asciidoc specific tests; unrelated
 * to the system functionality but related to the generation of the different formats
 * from asciidoc.
 */
public class AsciidocTest {

    /*
     * CCS-3186: Ensure tables with empty cells don't throw an error when using Pantheon's
     * custom HAML templates
     */
    @Test
    void testEmptyTableGeneration() {
        // Given
        String asciidocContent = "" +
                "# Asciidoctor with tables and empty cells\n" +
                "\n" +
                "This is a test asciidoctor\n" +
                "\n" +
                "|===\n" +
                "|Header 1 | Header 2 | Header 3\n" +
                "|Column 1 |          | Column 3\n" +
                "|===" +
                "";

        // When
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        // Then
        OptionsBuilder options = OptionsBuilder.options()
                .templateDir(
                        new File(getClass().getClassLoader().getResource("apps/pantheon/templates/haml").getFile()));
        assertDoesNotThrow(() -> asciidoctor.convert(asciidocContent, options.get()));
    }
}
