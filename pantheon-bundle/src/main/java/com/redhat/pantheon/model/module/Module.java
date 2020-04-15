package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.jcr.RepositoryException;
import java.util.Locale;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * The definition of a Module resource in the system.
 * Module's contains different versions for different languages.
 * <br/><br/>
 *
 * A module's structure in the JCR tree is as follows:
 * .../modulename
 *                       /en-US
 *                             /4
 *                                   /content
 *                                           /asciidoc
 *                                           /cachedHtml
 *                                   /metadata
 *                             /3
 *                                   /content
 *                                   /metadata
 *                             /2 (older - just for historical purposes)
 *                                   /content
 *                                   /metadata
 *                             /1
 *                                   /content
 *                                   /metadata
 */
@JcrPrimaryType("pant:module")
public interface Module extends WorkspaceChild {

    @Named("jcr:uuid")
    Field<String> uuid();

    default ModuleLocale getModuleLocale(Locale locale) {
        return getChild(locale.toString(), ModuleLocale.class);
    }

    default ModuleLocale getOrCreateModuleLocale(Locale locale) {
        return getOrCreateChild(locale.toString(), ModuleLocale.class);
    }

    default ModuleLocale createModuleLocale(Locale locale) {
        return createChild(locale.toString(), ModuleLocale.class);
    }

    default Optional<ModuleVersion> getDraftVersion(@Nonnull final Locale locale) {
        ModuleLocale moduleLocale = getModuleLocale(locale);
        if(moduleLocale != null) {
            try {
                return ofNullable( moduleLocale.draft().getReference() );
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
        return empty();
    }

    default Optional<ModuleVersion> getReleasedVersion(@Nonnull final Locale locale) {
        ModuleLocale moduleLocale = getModuleLocale(locale);
        if(moduleLocale != null) {
            try {
                return ofNullable( moduleLocale.released().getReference() );
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
        return empty();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The released content for a given locale
     */
    default Optional<Content> getReleasedContent(final Locale locale) {
        return getReleasedVersion(locale)
                .map(moduleVersion -> moduleVersion.content().get());
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The draft content for a given locale
     */
    default Optional<Content> getDraftContent(final Locale locale) {
        return getDraftVersion(locale)
                .map(moduleVersion -> moduleVersion.content().get());
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The released metadata for a given locale
     */
    default Optional<Metadata> getReleasedMetadata(final Locale locale) {
        return getReleasedVersion(locale)
                .map(moduleVersion -> moduleVersion.metadata().get());
    }

  /**
     *
     * @param locale the locale to fetch the acknowledgment status content
     * @return the  status data for a released version for a given locale
     */
    default Optional<AckStatus> getAcknowledgementStatus(final Locale locale) {
        return getReleasedVersion(locale)
                .map(moduleVersion -> moduleVersion.ackStatus().get());
    }

    /**
     *
     * @param locale the locale to fetch the status content
     * @return the  status data for a draft version for a given locale
     */
    default Optional<AckStatus> getDraftAcknowledgementStatus(final Locale locale) {
        return getDraftVersion(locale)
                .map(moduleVersion -> moduleVersion.ackStatus().get());
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The draft metadata for a given locale
     */
    default Optional<Metadata> getDraftMetadata(final Locale locale) {
        return getDraftVersion(locale)
                .map(moduleVersion -> moduleVersion.metadata().get());
    }
}
