package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.inject.Named;
import javax.jcr.RepositoryException;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.counting;

/**
 * A specific module locale node which houses all the versions for a specific language in the module.
 */
@JcrPrimaryType("pant:moduleLocale")
public interface ModuleLocale extends SlingModel {

    public static final String RELEASED = "released";
    public static final String DRAFT = "draft";

    @Named(RELEASED)
    Child<ModuleVersion> released();

    @Named(DRAFT)
    Child<ModuleVersion> draft();

    default ModuleVersion getVersion(String name) {
        return getChild(name, ModuleVersion.class);
    }

    default ModuleVersion getOrCreateVersion(String name) {
        return getOrCreateChild(name, ModuleVersion.class);
    }

    /**
     * Archives the latest released version. Renames the version to the next available archive name.
     * @return The newly archived {@link ModuleVersion}
     */
    default ModuleVersion archiveReleasedVersion() {
        if(released().get() == null) {
            // nothing to do
            return null;
        }

        // Rename 'released' to the next sequence number
        String archivedVersionName = generateNextArchiveVersionName();
        try {
            return SlingModels.rename(released().get(), archivedVersionName, ModuleVersion.class);
        } catch (RepositoryException e) {
            throw new RuntimeException("Problem archiving released version to '" + archivedVersionName + "'", e);
        }
    }

    default ModuleVersion releaseDraftVersion() {
        if(draft().get() == null) {
            // nothing to do
            return null;
        }

        // Rename 'draft' to 'released'
        try {
            return SlingModels.rename(draft().get(), RELEASED, ModuleVersion.class);
        } catch (RepositoryException e) {
            throw new RuntimeException("Problem releasing draft version at '" + draft().get().getPath() + "'", e);
        }
    }

    default ModuleVersion rollbackReleasedVersion() {
        if(released().get() == null) {
            // nothing to do
            return null;
        }

        try {
            // If there is no 'draft' version already, rename 'released' to 'draft'
            if(draft().get() == null) {
                return SlingModels.rename(released().get(), DRAFT, ModuleVersion.class);
            }
            // Otherwise, simply archive it
            else {
                return archiveReleasedVersion();
            }
        } catch (RepositoryException e) {
            throw new RuntimeException("Problem rolling back released version", e);
        }
    }

    default ModuleVersion createNextVersion() {
        // Generate a new version name
        return createChild(generateNextVersionName(), ModuleVersion.class);
    }

    default String generateNextVersionName() {
        return "" + (stream(this.getChildren()).collect(counting()) + 1);
    }

    default String generateNextArchiveVersionName() {
        // Get a count of the number of already archived versions
        long archivedVersionCount = stream(this.getChildren()).collect(counting());
        if(draft().get() != null) {
            archivedVersionCount--;
        }
        if(released().get() != null) {
            archivedVersionCount--;
        }

        return Long.toString(archivedVersionCount + 1);
    }
}
