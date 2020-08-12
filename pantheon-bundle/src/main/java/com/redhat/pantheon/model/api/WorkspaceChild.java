package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.workspace.Workspace;
import org.apache.sling.api.resource.Resource;

public interface WorkspaceChild extends SlingModel {

    default Workspace getWorkspace() {
        Resource workspace = this.getParent();
        while (workspace != null && !"pantheon/workspace".equals(workspace.getResourceType())) {
            workspace = workspace.getParent();
        }
        return workspace == null ? null : workspace.adaptTo(Workspace.class);
    }
}
