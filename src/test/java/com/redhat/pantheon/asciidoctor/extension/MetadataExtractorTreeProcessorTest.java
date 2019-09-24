package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.module.Metadata;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class MetadataExtractorTreeProcessorTest {

    private final SlingContext slingContext = new SlingContext();

    @Test
    void extractMetadata() {
        // Given
        slingContext.build()
                .resource("/content/module1/locales/en_US/1/metadata")
                .commit();
        Metadata metadata =
                new Metadata(
                    slingContext.resourceResolver().getResource(
                            "/content/module1/locales/en_US/1/metadata"));
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent = "= A title for content" +
                "\n" +
                "\n" +
                "This is the first paragraph which serves as abstract for a module";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertEquals("A title for content", metadata.title.get());
    }

    @Test
    void extractMetadataAbstractNotPresent() {
        // Given
        slingContext.build()
                .resource("/content/module1/locales/en_US/1/metadata")
                .commit();
        Metadata metadata =
                new Metadata(
                        slingContext.resourceResolver().getResource("/content/module1/locales/en_US/1/metadata"));
        Resource module = slingContext.resourceResolver().getResource("/content/module1");
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent = "= A title for content";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertNull(metadata.mAbstract.get());
    }

    @Test
    void extractHeadline() {
        // Given
        slingContext.build()
                .resource("/content/module1/locales/en_US/1/metadata")
                .commit();
        Metadata metadata =
                new Metadata(
                        slingContext.resourceResolver().getResource(
                                "/content/module1/locales/en_US/1/metadata"));
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent = "= A title for content" +
                "\n" +
                "\n" +
                "== Headline" +
                "\n" +
                "\n" +
                "This is the first section.";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertEquals("Headline", metadata.headline.get());
    }

    @Test
    void extractHeadlineNotPresent() {
        // Given
        slingContext.build()
                .resource("/content/module1/locales/en_US/1/metadata")
                .commit();
        Metadata metadata =
                new Metadata(
                        slingContext.resourceResolver().getResource(
                                "/content/module1/locales/en_US/1/metadata"));
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent = "= A title for content" +
                "\n";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertNull(metadata.headline.get());
    }
}
