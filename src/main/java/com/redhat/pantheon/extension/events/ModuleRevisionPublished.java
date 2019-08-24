package com.redhat.pantheon.extension.events;

import com.redhat.pantheon.extension.Event;

import javax.annotation.Nonnull;

public class ModuleRevisionPublished implements Event {

    private final String moduleRevisionPath;

    public ModuleRevisionPublished(@Nonnull String moduleRevisionPath) {
        this.moduleRevisionPath = moduleRevisionPath;
    }

    public String getModuleRevisionPath() {
        return moduleRevisionPath;
    }
}
