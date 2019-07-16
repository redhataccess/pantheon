package com.redhat.pantheon.model;

import com.google.common.collect.Streams;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.SlingResource;
import com.redhat.pantheon.util.function.FunctionalUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Locale;

import static com.redhat.pantheon.util.function.FunctionalUtils.toLastElement;

public class Module extends SlingResource {

    public final Child<Locales> locales = child("locales", Locales.class);

    public Module(@Nonnull Resource resource) {
        super(resource);
    }

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

    public static class ModuleLocale extends SlingResource {

        public final Child<Revisions> revisions = child("revisions", Revisions.class, "sling:OrderedFolder");

        public ModuleLocale(@Nonnull Resource resource) {
            super(resource);
        }
    }

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
