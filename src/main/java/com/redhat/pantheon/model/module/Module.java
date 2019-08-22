package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.ReferenceField;
import com.redhat.pantheon.model.api.SlingResource;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import org.apache.sling.api.resource.Resource;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import java.util.Locale;
import java.util.Optional;

import static com.google.common.collect.Streams.stream;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.counting;

/**
 * The definition of a Module resource in the system.
 * Module's contains different revisions for different languages.
 * <br/><br/>
 *
 * A module's structure in the JCR tree is as follows:
 * .../modulename
 *                       /en-US
 *                             /v4
 *                                   /content
 *                                           /asciidoc
 *                                           /cachedHtml
 *                                   /metadata
 *                             /v3
 *                                   /content
 *                                   /metadata
 *                             /v2 (older - just for historical purposes)
 *                                 /content
 *                             /v1
 *                                 /content
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

    public Optional<ModuleRevision> getDraftRevision(@Nonnull final Locale locale) {
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

    public Optional<ModuleRevision> getReleasedRevision(@Nonnull final Locale locale) {
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
        return getReleasedRevision(locale)
                .map(moduleRevision -> moduleRevision.content.get());
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The draft content for a given locale
     */
    public Optional<Content> getDraftContent(final Locale locale) {
        return getDraftRevision(locale)
                .map(moduleRevision -> moduleRevision.content.get());
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The released metadata for a given locale
     */
    public Optional<Metadata> getReleasedMetadata(final Locale locale) {
        return getReleasedRevision(locale)
                .map(moduleRevision -> moduleRevision.metadata.get());
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The draft metadata for a given locale
     */
    public Optional<Metadata> getDraftMetadata(final Locale locale) {
        return getDraftRevision(locale)
                .map(moduleRevision -> moduleRevision.metadata.get());
    }

    /**
     * A specific module locale node which houses all the revisions for a specific language in the module.
     */
    @JcrPrimaryType("sling:OrderedFolder")
    public static class ModuleLocale extends SlingResource {

        public final ReferenceField<ModuleRevision> released = referenceField("released", ModuleRevision.class);

        public final ReferenceField<ModuleRevision> draft = referenceField("draft", ModuleRevision.class);

        public ModuleRevision getRevision(String name) {
            return child(name, ModuleRevision.class).get();
        }

        public ModuleRevision getOrCreateRevision(String name) {
            return child(name, ModuleRevision.class).getOrCreate();
        }

        public ModuleRevision createNextRevision() {
            // Generate a new revision name
            return child(generateNextRevisionName(), ModuleRevision.class).create();
        }

        private String generateNextRevisionName() {
            return "v" + stream(this.getChildren()).collect(counting());
        }

        public ModuleLocale(@Nonnull Resource resource) {
            super(resource);
        }
    }
}
