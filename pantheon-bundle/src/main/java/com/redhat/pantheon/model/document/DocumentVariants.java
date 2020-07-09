package com.redhat.pantheon.model.document;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.OrderedFolder;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.inject.Named;
import java.util.Optional;
import java.util.stream.Stream;

import static com.redhat.pantheon.model.document.DocumentVariant.DEFAULT_VARIANT_NAME;

/**
 * @author Carlos Munoz
 */
@JcrPrimaryType("sling:OrderedFolder")
public interface DocumentVariants extends OrderedFolder {

    // The default variant
    @Named(DEFAULT_VARIANT_NAME)
    Child<DocumentVariant> defaultVariant();

    default Stream<DocumentVariant> getVariants() {
        return this.as(DocumentVariant.class);
    }

    default DocumentVariant getOrCreateVariant(String name) {
        return child(name, DocumentVariant.class).get();
    }

    default Child<DocumentVariant> variant(String name) {
        return child(name, DocumentVariant.class);
    }

    @Override
    DocumentLocale getParent();
}
