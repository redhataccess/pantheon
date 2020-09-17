package com.redhat.pantheon.servlet.util;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.ModelException;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentVariant;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashSet;
import java.util.Set;

public class ServletHelper {

    private static final Set<String> DOCUMENT_TYPES = new HashSet<>();
    private static final Set<String> VARIANT_TYPES = new HashSet<>();
    static {
        DOCUMENT_TYPES.add(PantheonConstants.RESOURCE_TYPE_ASSEMBLY);
        DOCUMENT_TYPES.add(PantheonConstants.RESOURCE_TYPE_MODULE);
        VARIANT_TYPES.add(PantheonConstants.RESOURCE_TYPE_ASSEMBLYVARIANT);
        VARIANT_TYPES.add(PantheonConstants.RESOURCE_TYPE_MODULEVARIANT);
    }

    public ServletHelper() {}

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

    public static Object resourceToModel(Resource r) {
        String resourceType = r.getResourceType();
        if (DOCUMENT_TYPES.contains(resourceType)) {
            return r.adaptTo(Document.class);
        } else if (VARIANT_TYPES.contains(resourceType)) {
            return r.adaptTo(DocumentVariant.class);
        } else {
            throw new ModelException("Attempted to transform " + r.getPath() + " into model class, but resource type was " + resourceType);
        }
    }
}
