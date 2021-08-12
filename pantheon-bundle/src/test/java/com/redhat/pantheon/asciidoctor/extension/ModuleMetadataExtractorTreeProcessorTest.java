package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.ModuleMetadata;
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
class ModuleMetadataExtractorTreeProcessorTest {

    private final SlingContext slingContext = new SlingContext();

    @Test
    void extractMetadata() {
        // Given
        slingContext.build()
                .resource("/content/module1/locales/en_US/1/metadata")
                .commit();
        ModuleMetadata metadata =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource(
                                "/content/module1/locales/en_US/1/metadata"),
                        ModuleMetadata.class);
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent = "= A title for content" +
                "\n" +
                "\n" +
                "[." + MetadataExtractorTreeProcessor.ROLE_ABSTRACT + "]\n" +
                "This is the module abstract";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertEquals("A title for content", metadata.title().get());
        assertEquals("This is the module abstract",  metadata.mAbstract().get());
    }

    @Test
    void extractMetadataAbstractNotPresent() {
        // Given
        slingContext.build()
                .resource("/content/module1/locales/en_US/1/metadata")
                .commit();
        ModuleMetadata metadata =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource(
                                "/content/module1/locales/en_US/1/metadata"),
                        ModuleMetadata.class);
        Resource module = slingContext.resourceResolver().getResource("/content/module1");
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent = "= A title for content";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertNull(metadata.mAbstract().get());
    }

    @Test
    void extractMetadataAbstractInferred() {
        // Given
        slingContext.build().resource("/content/module1/locales/en_US/1/metadata").commit();
        ModuleMetadata metadata =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource(
				"/content/module1/locales/en_US/1/metadata"),
			ModuleMetadata.class);
        Resource module = slingContext.resourceResolver().getResource("/content/module1");
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent = "// Some comment.\n" 
                                 + "\n" 
                                 + "[id='something_{context}']\n"
                                 + "= A title for content\n" 
                                 + "\n"
                                 + "This para is selected as the abstract even if the role is not set.\n" 
                                 + "\n"
                                 + "Some other text or block like a procedure.";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertEquals("A title for content", metadata.title().get());
        assertEquals("This para is selected as the abstract even if the role is not set.",
			metadata.mAbstract().get());
    }

    @Test
    void extractHeadline() {
        // Given
        slingContext.build()
                .resource("/content/module1/locales/en_US/1/metadata")
                .commit();
        ModuleMetadata metadata =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource(
                                "/content/module1/locales/en_US/1/metadata"),
                        ModuleMetadata.class);
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent = "include::shared/attributes.adoc[]\n" +
                "\n" +
                "= Test Asciidoc Book\n" +
                "Red Hat Documentation team <author@email.com>\n" +
                "\n" +
                "[[support]]\n" +
                "== Headline\n" +
                "\n" +
                "[[support-ticket]]\n" +
                "=== Logging a support request";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertEquals("Headline", metadata.headline().get());
    }

    /**
     * CCS-3176
     * This specific regression test makes sure that when the context property is not available in a given block,
     * the extension code doesn't fail. This has been observed when the content has a data list
     */
    @Test
    void extractHeadlineWithException() {
        // Given
        slingContext.build()
                .resource("/content/module1/locales/en_US/1/metadata")
                .commit();
        ModuleMetadata metadata =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource(
                                "/content/module1/locales/en_US/1/metadata"),
                        ModuleMetadata.class);
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent =
                "`GTC` — Generic Token Card.\n" +
                "\n" +
                "`Username`::\n" +
                "+\n" +
                "Enter the user name to be used in the authentication process.\n" +
                "\n" +
                "`Password`::\n" +
                "+\n" +
                "Enter the password to be used in the authentication process.\n" +
                "\n" +
                "[[bh-Configuring_Protected_EAP_PEAP_Settings]]\n" +
                "[discrete]\n";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertNull(metadata.headline().get());
    }

    @Test
    void extractHeadlineNotPresent() {
        // Given
        slingContext.build()
                .resource("/content/module1/locales/en_US/1/metadata")
                .commit();
        ModuleMetadata metadata =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource(
                                "/content/module1/locales/en_US/1/metadata"),
                        ModuleMetadata.class);
        MetadataExtractorTreeProcessor extension = new MetadataExtractorTreeProcessor(metadata);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry().treeprocessor(extension);
        final String adocContent = "= A title for content" +
                "\n";

        // When
        asciidoctor.load(adocContent, new HashMap<>());

        // Then
        assertNull(metadata.headline().get());
    }
}
