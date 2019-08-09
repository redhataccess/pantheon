package com.redhat.pantheon.model;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.SlingResource;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import org.apache.sling.api.resource.Resource;

import javax.annotation.Nonnull;
import java.util.Locale;

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
 *                             /metadata
 *                                      /draft
 *                                      /released
 *                             /content
 *                                     /draft
 *                                           /asciidoc
 *                                           /cachedHtml
 *                                     /released (latest is released)
 *                                           /asciidoc
 *                                           /cachedHtml
 *                                     /v2 (older - just for historical purposes)
 *                                           /asciidoc
 *                                           /cachedHtml
 *                                     /v1 (older - just for historical purposes)
 *                                           /asciidoc
 *                                           /cachedHtml
 */
@JcrPrimaryType("pant:module")
public class Module extends SlingResource {

    public final Child<Locales> locales = child("locales", Locales.class);

    public Module(@Nonnull Resource resource) {
        super(resource);
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The released content for a given locale
     */
    public ContentInstance getReleasedContentInstance(final Locale locale) {
        return locales.map(l -> l.getModuleLocale(locale == null ? DEFAULT_MODULE_LOCALE : locale))
                .map(moduleLocale -> moduleLocale.content.get())
                .map(content -> content.released.get())
                .get();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The draft content for a given locale
     */
    public ContentInstance getDraftContentInstance(final Locale locale) {
        return locales.map(l -> l.getModuleLocale(locale == null ? DEFAULT_MODULE_LOCALE : locale))
                .map(moduleLocale -> moduleLocale.content.get())
                .map(content -> content.draft.get())
                .get();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The released metadata for a given locale
     */
    public MetadataInstance getReleasedMetadataInstance(final Locale locale) {
        return locales.map(l -> l.getModuleLocale(locale == null ? DEFAULT_MODULE_LOCALE : locale))
                .map(moduleLocale -> moduleLocale.metadata.get())
                .map(content -> content.released.get())
                .get();
    }

    /**
     * @param locale The locale to fetch the content instance for.
     * @return The draft metadata for a given locale
     */
    public MetadataInstance getDraftMetadataInstance(final Locale locale) {
        return locales.map(l -> l.getModuleLocale(locale == null ? DEFAULT_MODULE_LOCALE : locale))
                .map(moduleLocale -> moduleLocale.metadata.get())
                .map(content -> content.draft.get())
                .get();
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

        public final Child<Metadata> metadata = child("metadata", Metadata.class);

        public final Child<Content> content = child("content", Content.class);

        public ModuleLocale(@Nonnull Resource resource) {
            super(resource);
        }
    }

    public static class Metadata extends SlingResource {

        public final Child<MetadataInstance> draft = child("draft", MetadataInstance.class);

        public final Child<MetadataInstance> released = child("released", MetadataInstance.class);

        public Metadata(Resource wrapped) {
            super(wrapped);
        }
    }

    public static class Content extends SlingResource {

        public final Child<ContentInstance> draft = child("draft", ContentInstance.class);

        public final Child<ContentInstance> released = child("released", ContentInstance.class);

        public Content(Resource wrapped) {
            super(wrapped);
        }
    }
}
