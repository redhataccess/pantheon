package com.redhat.pantheon.extension.events;

import javax.annotation.Nonnull;

/**
 * Event fired when a module version has been published.
 * Includes the module version path so it can be re-fetched in the
 * handlers if necessary.
 */
public class ModuleVersionUnpublishedEvent extends ModuleVersionPublishStateEvent {

    public ModuleVersionUnpublishedEvent(@Nonnull String moduleVersionPath) {
        super(moduleVersionPath);
    }
}
