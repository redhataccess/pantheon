package com.redhat.pantheon.jcr;


import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class JcrResources {
    private JcrResources() {
    }

    public static void rename(Resource resource, String newName) throws RepositoryException {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        Session session = resourceResolver.adaptTo(Session.class);
        session.move(resource.getPath(), resource.getParent().getPath() + "/" + newName);
    }
}
