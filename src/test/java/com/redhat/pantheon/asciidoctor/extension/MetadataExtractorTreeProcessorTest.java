package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.ModuleRevision;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class MetadataExtractorTreeProcessorTest {

    private final SlingContext slingContext = new SlingContext();

    @Test
    void extractMetadata() {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module")
                .commit();
        Resource moduleRevision = slingContext.resourceResolver().getResource("/content/module1");
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(new ModuleRevision(moduleRevision));
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent = "= A title for content" +
                "\n" +
                "\n" +
                "This is the first paragraph which serves as abstract for a module";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertEquals("A title for content", moduleRevision.getValueMap().get("jcr:title"));
        assertEquals("This is the first paragraph which serves as abstract for a module",
                moduleRevision.getValueMap().get("pant:abstract"));
    }

    @Test
    void extractMetadataAbstractNotPresent() {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module")
                .commit();
        Resource module = slingContext.resourceResolver().getResource("/content/module1");
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(new ModuleRevision(module));
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent = "= A title for content";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertFalse(module.getValueMap().containsKey("pant:abstract"));
    }
}