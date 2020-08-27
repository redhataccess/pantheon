package com.redhat.pantheon.servlet.util;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class ServletHelper {

    private ServletHelper() {}

    /**
     * Returns a Resource given uuid
     * @param uuid
     * @return a Resource
     * @throws RepositoryException
     */
    public static Resource getResourceByUuid(SlingHttpServletRequest request, String uuid) throws RepositoryException {
        Node foundNode = request.getResourceResolver()
                .adaptTo(Session.class)
                .getNodeByIdentifier(uuid);

        // turn the node back into a resource
        Resource foundResource = request.getResourceResolver()
                .getResource(foundNode.getPath());

        return foundResource;
    }
}
