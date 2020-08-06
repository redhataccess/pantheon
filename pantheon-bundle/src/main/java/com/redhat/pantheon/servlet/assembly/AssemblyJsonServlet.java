package com.redhat.pantheon.servlet.assembly;

import com.google.common.base.Charsets;
import com.redhat.pantheon.html.Html;
import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.assembly.Assembly;
import com.redhat.pantheon.model.assembly.AssemblyMetadata;
import com.redhat.pantheon.model.assembly.AssemblyVersion;
import com.redhat.pantheon.servlet.AbstractJsonSingleQueryServlet;
import com.redhat.pantheon.servlet.ServletUtils;
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
import java.util.*;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.model.assembly.AssemblyVariant.DEFAULT_VARIANT_NAME;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsLocale;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * Get operation to render a Released Assembly data in JSON format.
 * Only two parameters are expected in the Get request:
 * 1. locale - Optional; indicates the locale that the assembly content is in, defaulted to en-US
 * 2. assembly_id - indicates the uuid string which uniquely identifies an assembly
 *
 * The url to GET a request from the server is /api/assembly
 * Example: <server_url>/api/assembly?locale=en-us&assembly_id=xyz&variant=abc
 * The said url is accessible outside of the system without any authentication.
 *
 * @author A.P. Rajjshekhar
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet to facilitate GET operation which accepts locale and assembly uuid to output assembly data",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
// /api/assembly.json?assembly_id=${assemblyUuid}&locale=${localeId}&variant=${variantName}";
@SlingServletPaths(value = "/api/assembly")
public class AssemblyJsonServlet extends AbstractJsonSingleQueryServlet {
    public static final String PRODUCT_VERSION = "product_version";
    public static final String PRODUCT_NAME = "product_name";
    public static final String PRODUCT_LINK = "product_link";
    public static final String VANITY_URL_FRAGMENT = "vanity_url_fragment";
    public static final String SEARCH_KEYWORDS = "search_keywords";
    public static final String VIEW_URI = "view_uri";
    public static final String PORTAL_URL = "PORTAL_URL";

    private final Logger log = LoggerFactory.getLogger(AssemblyJsonServlet.class);

    @Override
    protected String getQuery(SlingHttpServletRequest request) {
        // Get the query parameter(s)
        String uuidParam = paramValue(request, "assembly_id", "");

        StringBuilder query = new StringBuilder("select * from [pant:assembly] as assembly WHERE assembly.[jcr:uuid] = '")
                .append(uuidParam)
                .append("'");
        return query.toString();
    }

    @Override
    protected boolean isValidResource(@Nonnull SlingHttpServletRequest request, @Nonnull Resource resource) {
        Locale locale = paramValueAsLocale(request, "locale", DEFAULT_MODULE_LOCALE);
        String variantName = paramValue(request, "variant", DEFAULT_VARIANT_NAME);
        Assembly assembly = resource.adaptTo(Assembly.class);
        Optional<AssemblyVersion> releasedRevision = assembly.getReleasedVersion(locale, variantName);
        return releasedRevision.isPresent();
    }

    //ToDo: Refactor map based to builder pattern based POJO backed response entity
    @Override
    protected Map<String, Object> resourceToMap(@Nonnull SlingHttpServletRequest request,
                                                @NotNull Resource resource) throws RepositoryException {
        Assembly assembly = resource.adaptTo(Assembly.class);

        Locale locale = paramValueAsLocale(request, "locale", DEFAULT_MODULE_LOCALE);
        String variantName = paramValue(request, "variant", DEFAULT_VARIANT_NAME);
        Optional<AssemblyMetadata> releasedMetadata = assembly.getReleasedMetadata(locale, variantName);
        Optional<FileResource> releasedContent = assembly.getReleasedContent(locale, variantName);
        Optional<AssemblyVersion> releasedRevision = assembly.getReleasedVersion(locale, variantName);

        Map<String, Object> assemblyMap = super.resourceToMap(request, resource);
        Map<String, Object> assemblyDetails = new HashMap<>();

        assemblyDetails.put("status", SC_OK);
        assemblyDetails.put("message", "Assembly Found");

        String resourcePath = resource.getPath();
        assemblyMap.put("locale", ServletUtils.toLanguageTag(locale));
        assemblyMap.put("revision_id", releasedRevision.get().getName());
        assemblyMap.put("title", releasedMetadata.get().title().get());
        assemblyMap.put("headline", releasedMetadata.get().getValueMap().containsKey("pant:headline") ? releasedMetadata.get().headline().get() : "");
        //assemblyMap.put("description", releasedMetadata.get().description().get());
        assemblyMap.put("content_type", "assembly");
        assemblyMap.put("date_published", releasedMetadata.get().getValueMap().containsKey("pant:datePublished") ? releasedMetadata.get().datePublished().get().toInstant().toString() : "");
        assemblyMap.put("status", "published");

        // Assume the path is something like: /content/<something>/my/resource/path
        assemblyMap.put("assembly_url_fragment", resourcePath.substring("/content/repositories/".length()));

        // Striping out the jcr: from key name
        String assemblyId = (String) assemblyMap.remove("jcr:uuid");
        assemblyMap.put("assembly_uuid", assemblyId);
        // Convert date string to UTC
        Date dateModified = new Date(resource.getResourceMetadata().getModificationTime());
        assemblyMap.put("date_modified", dateModified.toInstant().toString());
        // Return the body content of the assembly ONLY
        assemblyMap.put("body",
                Html.parse(Charsets.UTF_8.name())
                        .andThen(Html.getBody())
                        .apply(releasedContent.get().jcrContent().get().jcrData().get()));

        // Fields that are part of the spec and yet to be implemented
        // TODO Should either of these be the variant name?
        assemblyMap.put("context_url_fragment", "");
        assemblyMap.put("context_id", "");

        // Process productVersion from metadata
        // Making these arrays - in the future, we will have multi-product, so get the API right the first time
        List<Map> productList = new ArrayList<>();
        assemblyMap.put("products", productList);
        ProductVersion pv = releasedMetadata.get().productVersion().getReference();
        if (pv != null) {
            Map<String, String> productMap = new HashMap<>();
            productList.add(productMap);
            productMap.put(PRODUCT_VERSION, pv.name().get());
            productMap.put(PRODUCT_NAME, pv.getProduct().name().get());
            productMap.put(PRODUCT_LINK, "https://www.redhat.com/productlinkplaceholder");
        }

        // Process url_fragment from metadata
        String urlFragment = releasedMetadata.get().urlFragment().get() != null ? releasedMetadata.get().urlFragment().get() : "";
        if (!urlFragment.isEmpty()) {
            assemblyMap.put(VANITY_URL_FRAGMENT, urlFragment);
        }
        else {
            assemblyMap.put(VANITY_URL_FRAGMENT, "");
        }

        String searchKeywords = releasedMetadata.get().searchKeywords().get();
        if (searchKeywords != null && !searchKeywords.isEmpty()) {
            assemblyMap.put(SEARCH_KEYWORDS, searchKeywords.split(", *"));
        }
        else {
            assemblyMap.put(SEARCH_KEYWORDS, new String[] {});
        }

        // Process view_uri
        if (System.getenv(PORTAL_URL) != null) {
            String view_uri = System.getenv(PORTAL_URL) + "/topics/" + ServletUtils.toLanguageTag(locale) + "/" + assemblyId;
            assemblyMap.put(VIEW_URI, view_uri);
        }
        else {
            assemblyMap.put(VIEW_URI, "");
        }

        // remove unnecessary fields from the map
        assemblyMap.remove("jcr:lastModified");
        assemblyMap.remove("jcr:lastModifiedBy");
        assemblyMap.remove("jcr:createdBy");
        assemblyMap.remove("jcr:created");
        assemblyMap.remove("sling:resourceType");
        assemblyMap.remove("jcr:primaryType");

        // Adding assemblyMap to a parent assemblyDetails map
        assemblyDetails.put("assembly", assemblyMap);

        return assemblyDetails;
    }
}
