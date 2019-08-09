package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.SlingResource;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import org.apache.sling.api.resource.Resource;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Optional;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;

/**
 * The definition of a Module resource in the system.
 * Module's contains different revisions for different languages.
 * <br/><br/>
 *
 * A module's structure in the JCR tree is as follows:
 * .../modulename
 *               /locales
 *                       /en-US
 *                             /draft
 *                                   /content
 *                                           /asciidoc
 *                                           /cachedHtml
 *                                   /metadata
 *                             /released
 *                                   /content
 *                                   /metadata
 *                             /v2 (older - just for historical purposes)
 *                                 /content
 *                             /v1
 *                                 /content
 */
@JcrPrimaryType("pant:module")
public class Module extends SlingResource {

    public final Child<Locales> locales = child("locales", Locales.class);

    public Module(@Nonnull Resource resource) {
        super(resource);
    }

    public Optional<ModuleRevision> getDraftRevision(final Locale locale) {
        return locales.map(l -> l.getModuleLocale(locale == null ? DEFAULT_MODULE_LOCALE : locale))
                .map(moduleLocale -> moduleLocale.draft.get());
    }

    public Optional<ModuleRevision> getReleasedRevision(final Locale locale) {
        return locales.map(l -> l.getModuleLocale(locale == null ? DEFAULT_MODULE_LOCALE : locale))
                .map(moduleLocale -> moduleLocale.released.get());
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
     * An intermediary node which holds all available locales for this
     * module. Every locale is available as a child node named after the
     * locale code.
     */
    public static class Locales extends SlingResource {

        public Locales(@Nonnull Resource resource) {
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
    }

    /**
     * A specific module locale node which houses all the revisions for a specific language in the module.
     */
    public static class ModuleLocale extends SlingResource {

        public final Child<ModuleRevision> released = child("released", ModuleRevision.class);

        public final Child<ModuleRevision> draft = child("draft", ModuleRevision.class);

        // TODO add other methods for historical module revisions

        public ModuleLocale(@Nonnull Resource resource) {
            super(resource);
        }
    }
}
