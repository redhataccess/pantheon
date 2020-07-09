package com.redhat.pantheon.model.document;

import com.redhat.pantheon.jcr.JcrResources;
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

import java.util.Calendar;

import static com.google.common.collect.Streams.stream;
import static com.redhat.pantheon.jcr.JcrResources.rename;
import static java.util.stream.Collectors.counting;

/**
 * A specific Document variant node which houses all the versions for a specific language in the Document.
 */
public interface DocumentVariant extends WorkspaceChild {

    String DEFAULT_VARIANT_NAME = "DEFAULT";

    Child<? extends DocumentVersion> draft();

    Child<? extends DocumentVersion> released();

    @Override
    DocumentVariants getParent();

    // Since we are not storing historical versions anymore, the only needed ones are draft and released
    /*default DocumentVersion getVersion(String name) {
        return getChild(name, DocumentVersion.class);
    }*/

    // TODO Not sure we need this
    default DocumentVersion getOrCreateVersion(String name) {
        return child(name, DocumentVersion.class).get();
    }

    // TODO Not sure we need this
    default DocumentVersion createNextVersion() {
        // Generate a new version name
        return child(generateNextVersionName(), DocumentVersion.class).create();
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
            rename(draft().get(), "released");
            released().get()
                    .metadata().get()
                    .datePublished().set(Calendar.getInstance());
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
                rename(released().get(), "draft");
            } else {
                // released + draft
                released().get().delete();
            }

        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    default DocumentLocale getParentLocale() {
        return SlingModels.getModel(this.getParent().getParent(), DocumentLocale.class);
    }

}
