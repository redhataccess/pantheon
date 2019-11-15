package com.redhat.pantheon.servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.redhat.pantheon.servlet.ServletUtils.*;

/**
 * Simple servlet which aims to provide an internal way to quickly access resources by their UUID. This servlet is
 * read-only, and the returned content is in json format. This servlet also offers the capability of deep diving into
 * the resource's children by providing a 'depth' parameter.
 *
 * @author Carlos Munoz
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which allows querying of any object via their UUID",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/pantheon/internal/node.json")
public class GetObjectByUUID extends SlingSafeMethodsServlet {

    final String PARAM_UUID = "uuid";
    final String PARAM_DEPTH = "depth";
    final String PARAM_DEREFERENCE = "dereference";

    private SlingHttpServletRequest request;

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response)
            throws ServletException, IOException {
        this.request = request;

        String uuid = paramValue(request, PARAM_UUID);
        Long depth = paramValueAsLong(request, PARAM_DEPTH, 0L);
        String dereference = paramValue(request, PARAM_DEREFERENCE);

        if(isNullOrEmpty(uuid)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter '" + PARAM_UUID + "' must be provided");
            return;
        }
        if (depth < 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter '" + PARAM_DEPTH + "' must be >= 0");
            return;
        }
        Map<String, Set<String>> dereferenceMap = new HashMap<>();
        if (!isNullOrEmpty(dereference)) {
            try {
                dereferenceMap.putAll(buildDereferenceMap(dereference));
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter '" + PARAM_DEREFERENCE + "' must follow the pattern [resourceTypeA:]propertyA,[resourceTypeB:]propertyB,etc...");
            }
        }

        try {
            Resource foundResource = getResourceByUuid(uuid);
            Map<String, Object> payload = resourceToMapRecursive(foundResource, depth, dereferenceMap);
            writeAsJson(response, payload);
        } catch (ItemNotFoundException infex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (RepositoryException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Accepts incoming URL parameter "dereference" as a String and builds a Map of properties to dereference, based
     * off of the sling resource type that the property is found on. For example:
     * Input: "pantheon/moduleLocale:draft,pantheon/moduleLocale:released,pantheon/moduleVersion:product"
     * Output:
     *  {
     *      "pantheon/moduleLocale": ["draft","released"],
     *      "pantheon/moduleVersion": ["product"]
     *  }
     * @param dereference The incoming dereference specification
     * @return A Map of Sets containing the sling resource type as the Key, and the Set of properties to dereference
     *  for that resource type as the Value.
     */
    private Map<String, Set<String>> buildDereferenceMap(String dereference) {
        Map<String, Set<String>> map = new HashMap<>();
        for (String token : dereference.split(",")) {
            String[] pair = token.split(":", 2);
            String key = pair.length > 1 ? pair[0] : null;
            String value = pair.length > 1 ? pair[1] : pair[0];
            Set<String> props = map.get(key);
            if (props == null) {
                props = new HashSet<>();
                map.put(key, props);
            }
            props.add(value);
        }
        return map;
    }

    private Resource getResourceByUuid(String uuid) throws RepositoryException {
        Node foundNode = request.getResourceResolver()
                .adaptTo(Session.class)
                .getNodeByIdentifier(uuid);

        // turn the node back into a resource
        Resource foundResource = request.getResourceResolver()
                .getResource(foundNode.getPath());

        return foundResource;
    }

    private Map<String, Object> resourceToMapRecursive(Resource resource, Long depth, Map<String, Set<String>> dereferenceMap) throws RepositoryException {
        Map<String, Object> ret = new HashMap<>();
        ret.putAll(resource.getValueMap());

        if (depth > 0) {
            for (Resource child : resource.getChildren()) {
                ret.put(child.getName(), resourceToMapRecursive(child, depth - 1, dereferenceMap));
            }

            String type = resource.getResourceType();
            Set<String> referenceFieldsResourceSpecific = dereferenceMap.get(type);
            Set<String> referenceFieldsGlobal = dereferenceMap.get(null);
            for (Set<String> referenceFields : new Set[] { referenceFieldsGlobal, referenceFieldsResourceSpecific }) {
                if (referenceFields != null) {
                    for (Map.Entry<String, Object> entry : resource.getValueMap().entrySet()) {
                        if (referenceFields.contains(entry.getKey())) {
                            Resource child = getResourceByUuid((String) entry.getValue());
                            ret.put(entry.getKey(), resourceToMapRecursive(child, depth - 1, dereferenceMap));
                        }
                    }
                }
            }
        }

        /**
         * The reasoning behind this .remove() line is that I was running into an issue where the value of the jcr:data
         * field was binary. This interefered with something - can't quite remember what - but either the JSON converter
         * couldn't handle it, or the browser couldn't display it. We can remove this line if necessary in the future,
         * but be aware of the ramifications.
         *
         * We may have the same issue with other fields if they turn out to be binary, although I don't expect that we
         * will run into that scenario.
         */
        ret.remove("jcr:data");
        
        return ret;
    }
}
