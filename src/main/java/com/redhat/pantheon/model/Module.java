package com.redhat.pantheon.model;

import com.google.common.collect.Streams;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.SlingResource;
import org.apache.sling.api.resource.Resource;

import javax.annotation.Nonnull;
import java.util.Locale;

import static com.redhat.pantheon.util.function.FunctionalUtils.toLastElement;

/**
 * The definition of a Module resource in the system.
 * Module's contains different revisions for different languages.
 */
public class Module extends SlingResource {

    public final Child<Locales> locales = child("locales", Locales.class);

    public Module(@Nonnull Resource resource) {
        super(resource);
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

        public final Child<Revisions> revisions = child("revisions", Revisions.class, "sling:OrderedFolder");

        public ModuleLocale(@Nonnull Resource resource) {
            super(resource);
        }
    }

    /**
     * A container for all revision in for a locale and a module. Each child
     * resource is named after the revision name, and each child can be adapted
     * to a {@link ModuleRevision} resource.
     * This intermediary node currently holds no other purpose than to act as
     * a container for revisions.
     */
    public static class Revisions extends SlingResource {

        public Revisions(@Nonnull Resource resource) {
            super(resource);
        }

        public ModuleRevision getModuleRevision(String revisionName) {
            return child(revisionName, ModuleRevision.class).get();
        }

        public ModuleRevision getDefaultRevision() {
            // right now returns the latest
            return getLatestRevision();
        }

        public ModuleRevision getLatestRevision() {
            Resource latestRevResource = Streams.stream(getChildren())
                    .reduce(toLastElement())
                    .orElse(null);
            return latestRevResource == null ? null : new ModuleRevision(latestRevResource);
        }

        public ModuleRevision createModuleRevision(String revisionName) {
            return child(revisionName, ModuleRevision.class, "pant:moduleVersion").create();
        }

        public ModuleRevision getOrCreateModuleRevision(String revisionName) {
            return child(revisionName, ModuleRevision.class, "pant:moduleVersion").getOrCreate();
        }
    }
}
