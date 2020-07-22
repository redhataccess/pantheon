package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.annotation.Nonnull;
import javax.inject.Named;
import java.util.Locale;
import java.util.Optional;

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
        return (Optional<ModuleVersion>) Document.super.getDraftVersion(locale, variantName);
    }

    default Optional<ModuleVersion> getReleasedVersion(@Nonnull final Locale locale,
                                                       @Nonnull final String variantName) {
        return (Optional<ModuleVersion>) Document.super.getReleasedVersion(locale, variantName);
    }

    default Optional<ModuleMetadata> getDraftMetadata(@Nonnull final Locale locale,
                                                      @Nonnull final String variantName) {
        return (Optional<ModuleMetadata>) Document.super.getDraftMetadata(locale, variantName);
    }

    default Optional<ModuleMetadata> getReleasedMetadata(@Nonnull final Locale locale,
                                                         @Nonnull final String variantName) {
        return (Optional<ModuleMetadata>) Document.super.getReleasedMetadata(locale, variantName);
    }
}
