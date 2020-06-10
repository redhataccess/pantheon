package com.redhat.pantheon.servlet.module;

import com.google.common.base.Charsets;
import com.ibm.icu.util.ULocale;
import com.redhat.pantheon.html.Html;
import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.model.module.ModuleVersion;
import com.redhat.pantheon.servlet.AbstractJsonSingleQueryServlet;
import com.redhat.pantheon.servlet.ServletUtils;
import com.redhat.pantheon.servlet.util.SlingPathSuffix;
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

import static com.redhat.pantheon.conf.GlobalConfig.CONTENT_TYPE;
import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;
import static javax.servlet.http.HttpServletResponse.SC_OK;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet to facilitate GET operation which accepts locale and module uuid to output module data",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
// /api/module/variant.json/${variantUuid}";
@SlingServletPaths(value = "/api/module/variant")
public class VariantJsonServlet extends AbstractJsonSingleQueryServlet {
    public static final String PRODUCT_VERSION = "product_version";
    public static final String PRODUCT_NAME = "product_name";
    public static final String PRODUCT_LINK = "product_link";
    public static final String VANITY_URL_FRAGMENT = "vanity_url_fragment";
    public static final String SEARCH_KEYWORDS = "search_keywords";
    public static final String VIEW_URI = "view_uri";
    public static final String PORTAL_URL = "PORTAL_URL";

    private final Logger log = LoggerFactory.getLogger(ModuleJsonServlet.class);

    private final SlingPathSuffix suffix = new SlingPathSuffix("/{variantUuid}");

    @Override
    protected String getQuery(SlingHttpServletRequest request) {
        // Get the query parameter(s)
        String uuid = suffix.getParam("variantUuid", request);
        // Hydra fetch calls look like this:
        // Calling pantheon2 with url https://<HOST>/api/module/variant.json/b537ef3c-5c7d-4280-91ce-e7e818e6cc11&proxyHost=<SOMEHOST>&proxyPort=8080&throwExceptionOnFailure=false
        StringBuilder query = new StringBuilder("select * from [pant:moduleVariant] as moduleVariant WHERE moduleVariant.[jcr:uuid] = '")
                .append(sanitizeSuffix(uuid))
                .append("'");
        return query.toString();
    }

    @Override
    protected boolean isValidResource(@Nonnull SlingHttpServletRequest request, @Nonnull Resource resource) {
        ModuleVariant moduleVariant = resource.adaptTo(ModuleVariant.class);
        Optional<ModuleVersion> releasedRevision = moduleVariant != null ? Optional.ofNullable(moduleVariant.released().get()) : Optional.empty();

        return releasedRevision.isPresent();
    }

    @Override
    protected Map<String, Object> resourceToMap(@Nonnull SlingHttpServletRequest request,
                                                @NotNull Resource resource) throws RepositoryException {
        ModuleVariant moduleVariant = resource.adaptTo(ModuleVariant.class);
        Optional<Metadata> releasedMetadata = traverseFrom(moduleVariant)
                    .toChild(ModuleVariant::released)
                    .toChild(ModuleVersion::metadata)
                    .getAsOptional();
        Optional<FileResource> releasedContent = traverseFrom(moduleVariant)
                    .toChild(ModuleVariant::released)
                    .toChild(ModuleVersion::cachedHtml)
                    .getAsOptional();
        Optional<ModuleVersion> releasedRevision = traverseFrom(moduleVariant)
                    .toChild(ModuleVariant::released)
                    .getAsOptional();

        Map<String, Object> variantMap = super.resourceToMap(request, resource);
        Map<String, Object> variantDetails = new HashMap<>();

        variantDetails.put("status", SC_OK);
        variantDetails.put("message", "Module Found");

        String resourcePath = resource.getPath();
        Locale locale = ULocale.createCanonical(moduleVariant.getParentLocale().getName()).toLocale();
        variantMap.put("locale", ServletUtils.toLanguageTag(locale));
        variantMap.put("revision_id", releasedRevision.get().getName());
        variantMap.put("title", releasedMetadata.get().title().get());
        variantMap.put("headline", releasedMetadata.get().getValueMap().containsKey("pant:headline") ? releasedMetadata.get().headline().get() : "");
        variantMap.put("description", releasedMetadata.get().description().get());
        variantMap.put("content_type", CONTENT_TYPE);
        variantMap.put("date_published", releasedMetadata.get().getValueMap().containsKey("pant:datePublished") ? releasedMetadata.get().datePublished().get().toInstant().toString() : "");
        variantMap.put("status", "published");

        // Assume the path is something like: /content/<something>/my/resource/path
        variantMap.put("module_url_fragment", resourcePath.substring("/content/repositories/".length()));

        // Striping out the jcr: from key name
        String variant_uuid = (String) variantMap.remove("jcr:uuid");
        variantMap.put("variant_uuid", variant_uuid);
        // Convert date string to UTC
        Date dateModified = new Date(resource.getResourceMetadata().getModificationTime());
        variantMap.put("date_modified", dateModified.toInstant().toString());
        // Return the body content of the module ONLY
        variantMap.put("body",
                Html.parse(Charsets.UTF_8.name())
                        .andThen(Html.getBody())
                        .apply(releasedContent.get().jcrContent().get().jcrData().get()));

        // Fields that are part of the spec and yet to be implemented
        // TODO Should either of these be the variant name?
        variantMap.put("context_url_fragment", "");
        variantMap.put("context_id", "");

        // Process productVersion from metadata
        // Making these arrays - in the future, we will have multi-product, so get the API right the first time
        List<Map> productList = new ArrayList<>();
        variantMap.put("products", productList);
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
            variantMap.put(VANITY_URL_FRAGMENT, urlFragment);
        }
        else {
            variantMap.put(VANITY_URL_FRAGMENT, "");
        }

        String searchKeywords = releasedMetadata.get().searchKeywords().get();
        if (searchKeywords != null && !searchKeywords.isEmpty()) {
            variantMap.put(SEARCH_KEYWORDS, searchKeywords.split(", *"));
        }
        else {
            variantMap.put(SEARCH_KEYWORDS, new String[] {});
        }

        // Process view_uri
        if (System.getenv(PORTAL_URL) != null) {
            String view_uri = System.getenv(PORTAL_URL)
                    + "/topics/"
                    + ServletUtils.toLanguageTag(locale)
                    + "/" + variant_uuid;
            variantMap.put(VIEW_URI, view_uri);
        }
        else {
            variantMap.put(VIEW_URI, "");
        }

        // remove unnecessary fields from the map
        variantMap.remove("jcr:lastModified");
        variantMap.remove("jcr:lastModifiedBy");
        variantMap.remove("jcr:createdBy");
        variantMap.remove("jcr:created");
        variantMap.remove("sling:resourceType");
        variantMap.remove("jcr:primaryType");

        // Adding variantMap to a parent variantDetails map
        variantDetails.put("module", variantMap);

        return variantDetails;
    }

    private String sanitizeSuffix( String suffix) {
        // b537ef3c-5c7d-4280-91ce-e7e818e6cc11&proxyHost=<SOMEHOST>&proxyPort=8080&throwExceptionOnFailure=false

        if(suffix.contains("&")) {
            String parts[] = suffix.split("\\&");
            suffix = parts[0];
        }

        if(suffix.contains("?")) {
            String parts[] = suffix.split("\\?");
            suffix = parts[0];
        }

        return suffix;
    }
}
