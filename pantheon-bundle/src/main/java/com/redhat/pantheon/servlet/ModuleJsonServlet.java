package com.redhat.pantheon.servlet;

import com.google.common.base.Charsets;
import com.redhat.pantheon.html.Html;
import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.Content;
import com.redhat.pantheon.model.module.ModuleVersion;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.annotations.SlingServletPaths;

import org.jetbrains.annotations.NotNull;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.*;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.conf.GlobalConfig.CONTENT_TYPE;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * Get operation to render a Released Module data in JSON format.
 * Only two parameters are expected in the Get request:
 * 1. locale - Optional; indicates the locale that the module content is in, defaulted to en-US
 * 2. module_id - indicates the uuid string which uniquely identifies a module
 *
 * The url to GET a request from the server is /api/module
 * Example: <server_url>/api/module?locale=en-us&module_id=xyz
 * The said url is accessible outside of the system without any authentication.
 *
 * @author Ankit Gadgil
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet to facilitate GET operation which accepts locale and module uuid to output module data",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/api/module")
public class ModuleJsonServlet extends AbstractJsonSingleQueryServlet {
    private final Logger log = LoggerFactory.getLogger(ModuleJsonServlet.class);
    
    private SlingHttpServletRequest request;

    @Override
    protected String getQuery(SlingHttpServletRequest request) {
    	this.request = request;
        // Get the query parameter(s)
        String uuidParam = paramValue(request, "module_id", "");

        StringBuilder query = new StringBuilder("select * from [pant:module]")
                .append(" as module WHERE ")
                // look for a specific uuid for module
                .append("module.[jcr:uuid] = '" + uuidParam + "'");
        return query.toString();
    }

    @Override
    protected boolean isValidResource(@Nonnull Resource resource) {
        Module module = resource.adaptTo(Module.class);
        Optional<ModuleVersion> releasedRevision = module.getReleasedVersion(DEFAULT_MODULE_LOCALE);
        return releasedRevision.isPresent();
    }

    protected Map<String, Object> resourceToMap(@NotNull Resource resource) throws RepositoryException {
        Module module = resource.adaptTo(Module.class);

        // The DEFAULT_MODULE_LOCALE should later be replaced with 'localeParam' variable
        // this needs to be done while handling localizations
        Optional<Metadata> releasedMetadata = module.getReleasedMetadata(DEFAULT_MODULE_LOCALE);
        Optional<Content> releasedContent = module.getReleasedContent(DEFAULT_MODULE_LOCALE);
        Optional<ModuleVersion> releasedRevision = module.getReleasedVersion(DEFAULT_MODULE_LOCALE);

        Map<String, Object> moduleMap = super.resourceToMap(resource);
        Map<String, Object> moduleDetails = new HashMap<>();

        moduleDetails.put("status", SC_OK);
        moduleDetails.put("message", "Module Found");

        String resourcePath = resource.getPath();
        moduleMap.put("locale", module.getModuleLocale(DEFAULT_MODULE_LOCALE).getName());
        moduleMap.put("revision_id", releasedRevision.get().getName());
        moduleMap.put("title", releasedMetadata.get().title.get());
        moduleMap.put("headline", releasedMetadata.get().getValueMap().containsKey("pant:headline") ? releasedMetadata.get().headline.get() : "");
        moduleMap.put("description", releasedMetadata.get().description.get());
        moduleMap.put("content_type", CONTENT_TYPE);
        moduleMap.put("date_published", releasedMetadata.get().getValueMap().containsKey("pant:datePublished") ? releasedMetadata.get().datePublished.get().toInstant().toString() : "");

        // Assume the path is something like: /content/<something>/my/resource/path
        moduleMap.put("module_url_fragment", resourcePath.substring("/content/repositories/".length(), resourcePath.length()));

        // Striping out the jcr: from key name
        moduleMap.put("module_uuid", moduleMap.remove("jcr:uuid"));
        // Convert date string to UTC
        Date dateModified = new Date(resource.getResourceMetadata().getModificationTime());
        moduleMap.put("date_modified", dateModified.toInstant().toString());
        // Return the body content of the module ONLY
        moduleMap.put("body",
                Html.parse(Charsets.UTF_8.name())
                        .andThen(Html.getBody())
                        .apply(releasedContent.get().cachedHtml.get().data.get()));

        // Fields that are part of the spec and yet to be implemented
        moduleMap.put("context_url_fragment", "");
        moduleMap.put("context_id", "");
        moduleMap.put("product_name", "");
        moduleMap.put("product_version", "");
        
        // Process productVersion from metadata
        String versionUUID = releasedMetadata.get().getValueMap().containsKey("productVersion") ? releasedMetadata.get().productVersion.get() : "";
        if (!versionUUID.isEmpty()) {
        	try {
        		moduleMap.put("product_version", getResourceByUuid(versionUUID).getName());
        		moduleMap.put("product_name", getResourceByUuid(versionUUID).getParent().getParent().getName());
        	}  catch (RepositoryException e) {
                throw new RepositoryException(e);
            }
        }
        
        // Process url_fragment from metadata
        String urlFragment = releasedMetadata.get().getValueMap().containsKey("urlFragment") ? releasedMetadata.get().urlFragment.get() : "";
        if (!urlFragment.isEmpty()) {
        	moduleMap.put("vanity_url_fragment", urlFragment);
        }
        // remove unnecessary fields from the map
        moduleMap.remove("jcr:lastModified");
        moduleMap.remove("jcr:lastModifiedBy");
        moduleMap.remove("jcr:createdBy");
        moduleMap.remove("jcr:created");
        moduleMap.remove("sling:resourceType");
        moduleMap.remove("jcr:primaryType");

        // Adding moduleMap to a parent moduleDetails map
        moduleDetails.put("module", moduleMap);

        return moduleDetails;
    }
    
    private Resource getResourceByUuid(String uuid) throws ItemNotFoundException, RepositoryException {
        Node foundNode = request.getResourceResolver()
                .adaptTo(Session.class)
                .getNodeByIdentifier(uuid);

        // turn the node back into a resource
        Resource foundResource = request.getResourceResolver()
                .getResource(foundNode.getPath());

        return foundResource;
    }
}
