package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.annotation.Nonnull;
import javax.inject.Named;
import java.util.Locale;
import java.util.Optional;

@JcrPrimaryType("pant:assembly")
public interface Assembly extends Document {

    @Named("jcr:uuid")
    Field<String> uuid();

    default Child<AssemblyLocale> locale(Locale locale) {
        return child(locale.toString(), AssemblyLocale.class);
    }

    default Optional<AssemblyVersion> getDraftVersion(@Nonnull final Locale locale,
                                                      @Nonnull final String variantName) {
        return (Optional<AssemblyVersion>) Document.super.getDraftVersion(locale, variantName);
    }

    default Optional<AssemblyVersion> getReleasedVersion(@Nonnull final Locale locale,
                                                       @Nonnull final String variantName) {
        return (Optional<AssemblyVersion>) Document.super.getReleasedVersion(locale, variantName);
    }

    default Optional<AssemblyMetadata> getDraftMetadata(@Nonnull final Locale locale,
                                                      @Nonnull final String variantName) {
        return (Optional<AssemblyMetadata>) Document.super.getDraftMetadata(locale, variantName);
    }

    default Optional<AssemblyMetadata> getReleasedMetadata(@Nonnull final Locale locale,
                                                         @Nonnull final String variantName) {
        return (Optional<AssemblyMetadata>) Document.super.getReleasedMetadata(locale, variantName);
    }
}
