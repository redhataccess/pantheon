package com.redhat.pantheon.extension.events.assembly;

import com.redhat.pantheon.extension.Event;
import com.redhat.pantheon.model.assembly.AssemblyVersion;

import javax.annotation.Nonnull;

/**
 * Event fired when a assembly version has been published.
 * Includes the assembly version path so it can be re-fetched in the
 * handlers if necessary.
 */
public class AssemblyVersionPublishStateEvent implements Event {

    private final String moduleVersionPath;

    protected AssemblyVersionPublishStateEvent(@Nonnull AssemblyVersion assemblyVersion) {
        this.moduleVersionPath = assemblyVersion.getPath();
    }

    public String getModuleVersionPath() {
        return moduleVersionPath;
    }
}
