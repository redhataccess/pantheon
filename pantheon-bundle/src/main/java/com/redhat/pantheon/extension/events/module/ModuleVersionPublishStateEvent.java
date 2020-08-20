package com.redhat.pantheon.extension.events.module;

import com.redhat.pantheon.extension.Event;
import com.redhat.pantheon.model.module.ModuleVersion;

import javax.annotation.Nonnull;

/**
 * Event fired when a module version has been published.
 * Includes the module version path so it can be re-fetched in the
 * handlers if necessary.
 */
public class ModuleVersionPublishStateEvent implements Event {

    private final String moduleVersionPath;

    protected ModuleVersionPublishStateEvent(@Nonnull ModuleVersion moduleVersion) {
        this.moduleVersionPath = moduleVersion.getPath();
    }

    public String getModuleVersionPath() {
        return moduleVersionPath;
    }
}
