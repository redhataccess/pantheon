package com.redhat.pantheon.extension.events;

import com.redhat.pantheon.extension.Event;

import javax.annotation.Nonnull;

/**
 * Event fired when a module version has been published.
 * Includes the module version path so it can be re-fetched in the
 * handlers if necessary.
 */
public class ModuleVersionPublishStateEvent implements Event {

    private final String moduleLocalePath;

    protected ModuleVersionPublishStateEvent(@Nonnull String moduleLocalePath) {
        this.moduleLocalePath = moduleLocalePath;
    }

    public String getModuleLocalePath() {
        return moduleLocalePath;
    }
}
