package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.api.Reference;
import com.redhat.pantheon.model.api.SlingModel;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.counting;

/**
 * A specific module variant node which houses all the versions for a specific language in the module.
 */
@JcrPrimaryType("pant:moduleVariant")
public interface ModuleVariant extends WorkspaceChild {

    String DEFAULT_VARIANT_NAME = "DEFAULT";

    Child<ModuleVersion> draft();

    Child<ModuleVersion> released();

    @Override
    Variants getParent();

    // Since we are not storing historical versions anymore, the only needed ones are draft and released
    /*default ModuleVersion getVersion(String name) {
        return getChild(name, ModuleVersion.class);
    }*/

    // TODO Not sure we need this
    default ModuleVersion getOrCreateVersion(String name) {
        return getOrCreateChild(name, ModuleVersion.class);
    }

    // TODO Not sure we need this
    default ModuleVersion createNextVersion() {
        // Generate a new version name
        return createChild(generateNextVersionName(), ModuleVersion.class);
    }

    // TODO Not sure we need this
    default String generateNextVersionName() {
        return "draft";
    }

    default void releaseDraft() {
        if(draft().get() == null) {
            throw new RuntimeException("There is no draft to release");
        }

        try {
            // ensure the released version is discarded
            if( released().get() != null ) {
                getResourceResolver().delete(released().get());
            }
            // promote draft to released
            // (This uses the JCR API)
            getResourceResolver().adaptTo(Session.class)
                    .move(draft().get().getPath(), this.getPath() + "/released");
        } catch (PersistenceException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    default void revertReleased() {
        if(released().get() == null) {
            throw new RuntimeException("There is no released version to revert");
        }

        try {
            // if there is no draft version, set the recently unpublished one as draft
            // it is guaranteed to be the latest one
            if(draft().get() == null) {
                // FIXME This is a rundimentary "clone" operation since neither Sling nor JCR
                // offer an elegant, straightforward way to do this
                // 1. Create the draft revision as a clone of the released one
                Resource newDraft = getResourceResolver().create(released().get().getParent(), "draft", released().get().getValueMap());
                // 2. Copy all the children from the released one into draft
                released().get().getChildren().forEach(child -> {
                    try {
                        getResourceResolver().copy(child.getPath(), newDraft.getPath());
                    } catch (PersistenceException e) {
                        throw new RuntimeException(e);
                    }
                });
                getResourceResolver().copy(released().get().getPath(), this.getPath() + "/draft");
            }

            // Released revision is emptied out
            getResourceResolver().delete(released().get());
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    default ModuleLocale getParentLocale() {
        return SlingModels.getModel(this.getParent().getParent(), ModuleLocale.class);
    }

}
