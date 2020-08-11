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

    private static final String CUSTOM_HAML_TEMPLATES_PATH = "apps/pantheon/templates/haml";

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
        OptionsBuilder options = OptionsBuilder.options()
                .templateDir(
                        new File(getClass().getClassLoader().getResource(CUSTOM_HAML_TEMPLATES_PATH).getFile()));

        // When
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        // Then
        assertDoesNotThrow(() -> asciidoctor.convert(asciidocContent, options.get()));
    }
}
