package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.workspace.Workspace;
import org.apache.sling.api.resource.Resource;

public interface WorkspaceChild extends SlingModel {

    default Workspace getWorkspace() {
        Resource workspace = this.getParent();
        while (workspace != null && !workspace.getResourceType().equals("pantheon/workspace")) {
            workspace = workspace.getParent();
        }
        return workspace == null ? null : workspace.adaptTo(Workspace.class);
    }
}
