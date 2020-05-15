package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.api.util.ResourceTraversal;

import javax.annotation.Nonnull;
import javax.inject.Named;
import java.util.Locale;
import java.util.Optional;

import static com.redhat.pantheon.model.api.util.ResourceTraversal.start;

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
public interface Module extends WorkspaceChild {

    @Named("jcr:uuid")
    Field<String> uuid();

    @Deprecated
    /**
     * @deprecated use {@link Module#moduleLocale(Locale)} instead.
     */
    default ModuleLocale getModuleLocale(Locale locale) {
        return child(locale.toString(), ModuleLocale.class).get();
    }

    default Child<ModuleLocale> moduleLocale(Locale locale) {
        return child(locale.toString(), ModuleLocale.class);
    }

    default ModuleLocale getOrCreateModuleLocale(Locale locale) {
        return child(locale.toString(), ModuleLocale.class).getOrCreate();
    }

    default ModuleLocale createModuleLocale(Locale locale) {
        return child(locale.toString(), ModuleLocale.class).create();
    }

    default Optional<ModuleVersion> getDraftVersion(@Nonnull final Locale locale,
                                                    @Nonnull final String variantName) {
        return start(this)
                .traverse(m -> moduleLocale(locale))
                .traverse(ModuleLocale::variants)
                .traverse(variants -> variants.variant(variantName))
                .traverse(ModuleVariant::draft)
                .getAsOptional();
    }

    default Optional<ModuleVersion> getReleasedVersion(@Nonnull final Locale locale,
                                                    @Nonnull final String variantName) {
        return start(this)
                .traverse(m -> moduleLocale(locale))
                .traverse(ModuleLocale::variants)
                .traverse(variants -> variants.variant(variantName))
                .traverse(ModuleVariant::released)
                .getAsOptional();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @param variantName
     * @return The released content for a given locale
     */
    default Optional<FileResource> getReleasedContent(final Locale locale,
                                                      @Nonnull final String variantName) {
        return start(this)
                .traverse(m -> moduleLocale(locale))
                .traverse(ModuleLocale::variants)
                .traverse(variants -> variants.variant(variantName))
                .traverse(ModuleVariant::released)
                .traverse(ModuleVersion::cachedHtml)
                .getAsOptional();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @param variantName
     * @return The draft content for a given locale
     */
    default Optional<FileResource> getDraftContent(final Locale locale,
                                                 @Nonnull final String variantName) {
        return start(this)
                .traverse(m -> moduleLocale(locale))
                .traverse(ModuleLocale::variants)
                .traverse(variants -> variants.variant(variantName))
                .traverse(ModuleVariant::draft)
                .traverse(ModuleVersion::cachedHtml)
                .getAsOptional();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @param variantName
     * @return The released metadata for a given locale
     */
    default Optional<Metadata> getReleasedMetadata(final Locale locale,
                                                   @Nonnull final String variantName) {
        return start(this)
                .traverse(m -> moduleLocale(locale))
                .traverse(ModuleLocale::variants)
                .traverse(variants -> variants.variant(variantName))
                .traverse(ModuleVariant::released)
                .traverse(ModuleVersion::metadata)
                .getAsOptional();
    }

  /**
    *
    * @param locale the locale to fetch the acknowledgment status content
    * @param variantName
    * @return the  status data for a released version for a given locale
    */
    default Optional<AckStatus> getAcknowledgementStatus(final Locale locale,
                                                         @Nonnull final String variantName) {
        return start(this)
                .traverse(m -> moduleLocale(locale))
                .traverse(ModuleLocale::variants)
                .traverse(variants -> variants.variant(variantName))
                .traverse(ModuleVariant::released)
                .traverse(ModuleVersion::ackStatus)
                .getAsOptional();
    }

    /**
     *
     * @param locale the locale to fetch the status content
     * @param variantName
     * @return the  status data for a draft version for a given locale
     */
    default Optional<AckStatus> getDraftAcknowledgementStatus(final Locale locale,
                                                              @Nonnull final String variantName) {
        return start(this)
                .traverse(m -> moduleLocale(locale))
                .traverse(ModuleLocale::variants)
                .traverse(variants -> variants.variant(variantName))
                .traverse(ModuleVariant::draft)
                .traverse(ModuleVersion::ackStatus)
                .getAsOptional();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @param variantName
     * @return The draft metadata for a given locale
     */
    default Optional<Metadata> getDraftMetadata(final Locale locale,
                                                @Nonnull final String variantName) {
        return start(this)
                .traverse(m -> moduleLocale(locale))
                .traverse(ModuleLocale::variants)
                .traverse(variants -> variants.variant(variantName))
                .traverse(ModuleVariant::draft)
                .traverse(ModuleVersion::metadata)
                .getAsOptional();
    }
}
