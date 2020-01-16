package com.redhat.pantheon.servlet;

import com.google.common.base.Charsets;
import com.redhat.pantheon.html.Html;
import com.redhat.pantheon.model.module.Content;
import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVersion;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.redhat.pantheon.conf.GlobalConfig.CONTENT_TYPE;
import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsLocale;
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
    public static final String PRODUCT_VERSION = "product_version";
    public static final String PRODUCT_NAME = "product_name";
    public static final String VANITY_URL_FRAGMENT = "vanity_url_fragment";
    public static final String SEARCH_KEYWORDS = "search_keywords";
    public static final String VIEW_URI = "view_uri";
    public static final String PORTAL_URL = "PORTAL_URL";

    private final Logger log = LoggerFactory.getLogger(ModuleJsonServlet.class);

    @Override
    protected String getQuery(SlingHttpServletRequest request) {

        // Get the query parameter(s)
        String uuidParam = paramValue(request, "module_id", "");

        StringBuilder query = new StringBuilder("select * from [pant:module]")
                .append(" as module WHERE ")
                // look for a specific uuid for module
                .append("module.[jcr:uuid] = '" + uuidParam + "'");
        return query.toString();
    }

    @Override
    protected boolean isValidResource(@Nonnull SlingHttpServletRequest request, @Nonnull Resource resource) {
        Locale locale = paramValueAsLocale(request, "locale", DEFAULT_MODULE_LOCALE);
        Module module = resource.adaptTo(Module.class);
        Optional<ModuleVersion> releasedRevision = module.getReleasedVersion(locale);
        return releasedRevision.isPresent();
    }

    @Override
    protected Map<String, Object> resourceToMap(@Nonnull SlingHttpServletRequest request,
                                                @NotNull Resource resource) throws RepositoryException {
        Module module = resource.adaptTo(Module.class);

        Locale locale = paramValueAsLocale(request, "locale", DEFAULT_MODULE_LOCALE);
        Optional<Metadata> releasedMetadata = module.getReleasedMetadata(locale);
        Optional<Content> releasedContent = module.getReleasedContent(locale);
        Optional<ModuleVersion> releasedRevision = module.getReleasedVersion(locale);

        Map<String, Object> moduleMap = super.resourceToMap(request, resource);
        Map<String, Object> moduleDetails = new HashMap<>();

        moduleDetails.put("status", SC_OK);
        moduleDetails.put("message", "Module Found");

        String resourcePath = resource.getPath();
        moduleMap.put("locale", ServletUtils.toLanguageTag(locale));
        moduleMap.put("revision_id", releasedRevision.get().getName());
        moduleMap.put("title", releasedMetadata.get().title().get());
        moduleMap.put("headline", releasedMetadata.get().getValueMap().containsKey("pant:headline") ? releasedMetadata.get().headline().get() : "");
        moduleMap.put("description", releasedMetadata.get().description().get());
        moduleMap.put("content_type", CONTENT_TYPE);
        moduleMap.put("date_published", releasedMetadata.get().getValueMap().containsKey("pant:datePublished") ? releasedMetadata.get().datePublished().get().toInstant().toString() : "");
        moduleMap.put("status", "published");

        // Assume the path is something like: /content/<something>/my/resource/path
        moduleMap.put("module_url_fragment", resourcePath.substring("/content/repositories/".length(), resourcePath.length()));

        // Striping out the jcr: from key name
        String module_uuid = (String) moduleMap.remove("jcr:uuid");
        moduleMap.put("module_uuid", module_uuid);
        // Convert date string to UTC
        Date dateModified = new Date(resource.getResourceMetadata().getModificationTime());
        moduleMap.put("date_modified", dateModified.toInstant().toString());
        // Return the body content of the module ONLY
        moduleMap.put("body",
                Html.parse(Charsets.UTF_8.name())
                        .andThen(Html.getBody())
                        .apply(releasedContent.get().cachedHtml().get().data().get()));

        // Fields that are part of the spec and yet to be implemented
        moduleMap.put("context_url_fragment", "");
        moduleMap.put("context_id", "");
        moduleMap.put(PRODUCT_NAME, "");
        moduleMap.put(PRODUCT_VERSION, "");
        moduleMap.put(VANITY_URL_FRAGMENT, "");
        moduleMap.put(SEARCH_KEYWORDS, new String[] {});
        moduleMap.put(VIEW_URI, "");

        // Process productVersion from metadata
        String productVersion = releasedMetadata.get().productVersion().get() != null ? releasedMetadata.get().productVersion().getReference().name().get() : "";
        if (!productVersion.isEmpty()) {
            try {
                moduleMap.put(PRODUCT_VERSION, productVersion);
                moduleMap.put(PRODUCT_NAME, releasedMetadata.get().productVersion().getReference().getParent().getParent().getValueMap().get("name", String.class));
            }  catch (RepositoryException e) {
                log.error(e.getMessage());
            }
        }

        // Process url_fragment from metadata
        String urlFragment = releasedMetadata.get().urlFragment().get() != null ? releasedMetadata.get().urlFragment().get() : "";
        if (!urlFragment.isEmpty()) {
            moduleMap.put(VANITY_URL_FRAGMENT, urlFragment);
        }

        String searchKeywords = releasedMetadata.get().searchKeywords().get();
        if (searchKeywords != null && !searchKeywords.isEmpty()) {
            moduleMap.put(SEARCH_KEYWORDS, searchKeywords.split(", *"));
        }

        // Process view_uri
        if (System.getenv(PORTAL_URL) != null) {
            String view_uri = System.getenv(PORTAL_URL) + "/topics/" + ServletUtils.toLanguageTag(locale) + "/" + module_uuid;
            moduleMap.put(VIEW_URI, view_uri);
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
}
