package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.SlingResource;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import org.apache.sling.api.resource.Resource;

import javax.annotation.Nonnull;
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
public class Module extends SlingResource {

    public Module(@Nonnull Resource resource) {
        super(resource);
    }

    public ModuleLocale getModuleLocale(Locale locale) {
        return child(locale.toString(), ModuleLocale.class).get();
    }

    public ModuleLocale getOrCreateModuleLocale(Locale locale) {
        return child(locale.toString(), ModuleLocale.class).getOrCreate();
    }

    public ModuleLocale createModuleLocale(Locale locale) {
        return child(locale.toString(), ModuleLocale.class).create();
    }

    public Optional<ModuleVersion> getDraftVersion(@Nonnull final Locale locale) {
        ModuleLocale moduleLocale = getModuleLocale(locale);
        if(moduleLocale != null) {
            try {
                return ofNullable( moduleLocale.draft.getReference() );
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
        return empty();
    }

    public Optional<ModuleVersion> getReleasedVersion(@Nonnull final Locale locale) {
        ModuleLocale moduleLocale = getModuleLocale(locale);
        if(moduleLocale != null) {
            try {
                return ofNullable( moduleLocale.released.getReference() );
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
    public Optional<Content> getReleasedContent(final Locale locale) {
        return getReleasedVersion(locale)
                .map(moduleVersion -> moduleVersion.content.get());
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The draft content for a given locale
     */
    public Optional<Content> getDraftContent(final Locale locale) {
        return getDraftVersion(locale)
                .map(moduleVersion -> moduleVersion.content.get());
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The released metadata for a given locale
     */
    public Optional<Metadata> getReleasedMetadata(final Locale locale) {
        return getReleasedVersion(locale)
                .map(moduleVersion -> moduleVersion.metadata.get());
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The draft metadata for a given locale
     */
    public Optional<Metadata> getDraftMetadata(final Locale locale) {
        return getDraftVersion(locale)
                .map(moduleVersion -> moduleVersion.metadata.get());
    }
}
