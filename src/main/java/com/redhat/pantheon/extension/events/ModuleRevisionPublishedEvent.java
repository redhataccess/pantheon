package com.redhat.pantheon.extension.events;

import com.redhat.pantheon.extension.Event;

import javax.annotation.Nonnull;

/**
 * Event fired when a module revision has been published.
 * Includes the module revision path so it can be re-fetched in the
 * handlers if necessary.
 */
public class ModuleRevisionPublishedEvent implements Event {

    private final String moduleRevisionPath;

    public ModuleRevisionPublishedEvent(@Nonnull String moduleRevisionPath) {
        this.moduleRevisionPath = moduleRevisionPath;
    }

    public String getModuleRevisionPath() {
        return moduleRevisionPath;
    }
}
