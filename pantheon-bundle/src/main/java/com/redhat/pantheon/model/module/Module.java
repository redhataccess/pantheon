package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.annotation.Nonnull;
import javax.inject.Named;
import java.util.Locale;
import java.util.Optional;

import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;

/**
 * The definition of a Module resource in the system.
 * Module's contains different versions for different languages.
 * <br/><br/>
 *
 * A module's structure in the JCR tree is as follows:
 * .../modulename
 *      en-US
 *              sources
 *                      draft (as a file)
 *                              jcr:content
 *                      released (as a file)
 *                              jcr:content
 *              variants
 *                      VARIANT NAME (variants) //default value: DEFAULT
 *                              attrs file: /attributes/RHEL-7-atts.adoc
 *                              draft
 *                                      cachedHtml
 *                                      metadata
 *                                      ackStatus
 *                              released
 *                                      cachedHtml
 *                                      metadata
 *                                      ackStatus
 */
@JcrPrimaryType("pant:module")
public interface Module extends Document {

    @Named("jcr:uuid")
    Field<String> uuid();

    default Child<ModuleLocale> locale(Locale locale) {
        return child(locale.toString(), ModuleLocale.class);
    }

    default Optional<ModuleVersion> getDraftVersion(@Nonnull final Locale locale,
                                                    @Nonnull final String variantName) {
        return traverseFrom(this)
                .toChild(m -> m.locale(locale))
                .toChild(ModuleLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(ModuleVariant::draft)
                .getAsOptional();
    }

    default Optional<ModuleVersion> getReleasedVersion(@Nonnull final Locale locale,
                                                       @Nonnull final String variantName) {
        return traverseFrom(this)
                .toChild(m -> m.locale(locale))
                .toChild(ModuleLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(ModuleVariant::released)
                .getAsOptional();
    }

    default Optional<FileResource> getDraftContent(@Nonnull final Locale locale,
                                                   @Nonnull final String variantName) {
        return traverseFrom(this)
                .toChild(m -> m.locale(locale))
                .toChild(ModuleLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(ModuleVariant::draft)
                .toChild(ModuleVersion::cachedHtml)
                .getAsOptional();
    }

    default Optional<FileResource> getReleasedContent(@Nonnull final Locale locale,
                                                      @Nonnull final String variantName) {
        return traverseFrom(this)
                .toChild(m -> m.locale(locale))
                .toChild(ModuleLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(ModuleVariant::released)
                .toChild(ModuleVersion::cachedHtml)
                .getAsOptional();
    }

    default Optional<ModuleMetadata> getDraftMetadata(@Nonnull final Locale locale,
                                                      @Nonnull final String variantName) {
        return traverseFrom(this)
                .toChild(m -> m.locale(locale))
                .toChild(ModuleLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(ModuleVariant::draft)
                .toChild(ModuleVersion::metadata)
                .getAsOptional();
    }

    default Optional<ModuleMetadata> getReleasedMetadata(@Nonnull final Locale locale,
                                                         @Nonnull final String variantName) {
        return traverseFrom(this)
                .toChild(m -> m.locale(locale))
                .toChild(ModuleLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(ModuleVariant::released)
                .toChild(ModuleVersion::metadata)
                .getAsOptional();
    }
}
