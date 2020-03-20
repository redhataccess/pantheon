package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.Resource;

public interface WorkspaceChild extends SlingModel {

    default Resource getWorkspace() {
        Resource workspace = this.getParent();
        while (workspace != null && !workspace.getResourceType().equals("pant:workspace")) {
            workspace = workspace.getParent();
        }
        return workspace;
    }
}
