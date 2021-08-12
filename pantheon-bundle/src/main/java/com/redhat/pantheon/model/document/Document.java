package com.redhat.pantheon.model.document;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.workspace.WorkspaceChild;
import com.redhat.pantheon.validation.model.Validation;
import com.redhat.pantheon.validation.model.Validations;

import javax.annotation.Nonnull;
import javax.inject.Named;
import java.util.Locale;
import java.util.Optional;

public interface Document extends WorkspaceChild {

    @Named("jcr:uuid")
    Field<String> uuid();

    default Child<? extends DocumentLocale> locale(Locale locale) {
        return locale(locale.toString());
    }

    default Child<? extends DocumentLocale> locale(String locale) {
        return child(locale, DocumentLocale.class);
    }

    default Optional<? extends DocumentVersion> getDraftVersion(@Nonnull final Locale locale,
                                                                @Nonnull final String variantName) {
        return Child.from(this)
                .toChild(m -> m.locale(locale))
                .toChild(DocumentLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(DocumentVariant::draft)
                .asOptional();
    }

    default Optional<? extends DocumentVersion> getReleasedVersion(@Nonnull final Locale locale,
                                                                   @Nonnull final String variantName) {
        return Child.from(this)
                .toChild(m -> m.locale(locale))
                .toChild(DocumentLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(DocumentVariant::released)
                .asOptional();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @param variantName
     * @return The draft content for a given locale
     */
    default Optional<FileResource> getDraftContent(@Nonnull final Locale locale,
                                                   @Nonnull final String variantName) {
        return Child.from(this)
                .toChild(m -> m.locale(locale))
                .toChild(DocumentLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(DocumentVariant::draft)
                .toChild(DocumentVersion::cachedHtml)
                .asOptional();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @param variantName
     * @return The released content for a given locale
     */
    default Optional<FileResource> getReleasedContent(@Nonnull final Locale locale,
                                                      @Nonnull final String variantName) {
        return Child.from(this)
                .toChild(m -> m.locale(locale))
                .toChild(DocumentLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(DocumentVariant::released)
                .toChild(DocumentVersion::cachedHtml)
                .asOptional();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @param variantName
     * @return The draft metadata for a given locale
     */
    default Optional<? extends DocumentMetadata> getDraftMetadata(@Nonnull final Locale locale,
                                                                  @Nonnull final String variantName) {
        return Child.from(this)
                .toChild(m -> m.locale(locale))
                .toChild(DocumentLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(DocumentVariant::draft)
                .toChild(DocumentVersion::metadata)
                .asOptional();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @param variantName
     * @return The released metadata for a given locale
     */
    default Optional<? extends DocumentMetadata> getReleasedMetadata(@Nonnull final Locale locale,
                                                                     @Nonnull final String variantName) {
        return Child.from(this)
                .toChild(m -> m.locale(locale))
                .toChild(DocumentLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(DocumentVariant::released)
                .toChild(DocumentVersion::metadata)
                .asOptional();
    }

    /**
     *
     * @param locale the locale to fetch the acknowledgment status content
     * @param variantName
     * @return the  status data for a released version for a given locale
     */
    default Optional<AckStatus> getAcknowledgementStatus(@Nonnull final Locale locale,
                                                         @Nonnull final String variantName) {
        return Child.from(this)
                .toChild(m -> m.locale(locale))
                .toChild(DocumentLocale::variants)
                .toChild(variants -> variants.variant(variantName))
                .toChild(DocumentVariant::released)
                .toChild(DocumentVersion::ackStatus)
                .asOptional();
    }

    /**
     *
     * @param locale the locale to fetch the validations content
     * @param variantName
     * @param versionType
     * @return the validations data for draft/released version for a given locale
     */
    default Optional<Validations> getValidations(@Nonnull final Locale locale,
                                                 @Nonnull final String variantName,
                                                 @Nonnull final String versionType) {
        if (versionType.equalsIgnoreCase("draft")){
            return Child.from(this)
                    .toChild(m -> m.locale(locale))
                    .toChild(DocumentLocale::variants)
                    .toChild(variants -> variants.variant(variantName))
                    .toChild(DocumentVariant::draft)
                    .toChild(DocumentVersion::validations)
                    .asOptional();
        } else {
            return Child.from(this)
                    .toChild(m -> m.locale(locale))
                    .toChild(DocumentLocale::variants)
                    .toChild(variants -> variants.variant(variantName))
                    .toChild(DocumentVariant::released)
                    .toChild(DocumentVersion::validations)
                    .asOptional();
        }
    }


}
