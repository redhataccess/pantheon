package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.Resource;

public interface WorkspaceChild extends SlingModel {

    default Resource getWorkspace() {
        Resource workspace = this.getParent();
        System.out.println("Beginning workspace type: " + workspace.getResourceType());
        while (workspace != null && !workspace.getResourceType().equals("pantheon/workspace")) {
            workspace = workspace.getParent();
            System.out.println("New workspace type: " + workspace.getResourceType());
        }
        return workspace;
    }
}
