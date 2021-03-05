package com.redhat.pantheon.servlet;

import com.google.common.base.Charsets;
import com.redhat.pantheon.extension.url.CustomerPortalUrlUuidProvider;
import com.redhat.pantheon.html.Html;
import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.module.ModuleMetadata;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.model.module.ModuleVersion;
import com.redhat.pantheon.servlet.util.ServletHelper;
import org.apache.sling.api.SlingHttpServletRequest;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.conf.GlobalConfig.CONTENT_TYPE;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * A series of converters to map form for different business purposes.
 */
public class MapConverters {

    private MapConverters() {
    }

    /**
     * Converts a {@link ModuleVariant} object to a map for returning in API calls.
     * @param request The web request being processed.
     * @param mv The module variant domain object to transform to a map.
     * @return A map for Json conversion in API calls with the module variant's information.
     * @throws RepositoryException IF there is a problem fetching related data when building the map.
     */
    public static final Map<String, Object> moduleVariantToMap(final SlingHttpServletRequest request,
                                                               final ModuleVariant mv)
            throws RepositoryException {
        Optional<ModuleMetadata> releasedMetadata = mv.released()
                .toChild(ModuleVersion::metadata)
                .asOptional();
        Optional<FileResource> releasedContent = mv.released()
                .toChild(ModuleVersion::cachedHtml)
                .asOptional();
        Optional<ModuleVersion> releasedRevision = mv.released()
                .asOptional();

        Map<String, Object> variantMap = newHashMap(mv.getValueMap());
        Map<String, Object> variantDetails = new HashMap<>();

        variantDetails.put("status", SC_OK);
        variantDetails.put("message", "Module Found");

        String resourcePath = mv.getPath();
        variantMap.put("locale", ServletUtils.toLanguageTag(mv.getParentLocale().getName()));
        variantMap.put("revision_id", releasedRevision.get().getName());
        variantMap.put("title", releasedMetadata.get().title().get());
        variantMap.put("headline", releasedMetadata.get().getValueMap().containsKey("pant:headline") ? releasedMetadata.get().headline().get() : "");
        variantMap.put("description", releasedMetadata.get().getValueMap().containsKey("jcr:description") ? releasedMetadata.get().description().get() : releasedMetadata.get().mAbstract().get());
        variantMap.put("content_type", CONTENT_TYPE);
        variantMap.put("date_published", releasedMetadata.get().getValueMap().containsKey("pant:datePublished") ? releasedMetadata.get().datePublished().get().toInstant().toString() : "");
        variantMap.put("date_first_published", releasedMetadata.get().getValueMap().containsKey("pant:dateFirstPublished") ? releasedMetadata.get().dateFirstPublished().get().toInstant().toString() : "");
        variantMap.put("status", "published");

        // Assume the path is something like: /content/<something>/my/resource/path
        variantMap.put("module_url_fragment", resourcePath.substring("/content/repositories/".length()));

        // Striping out the jcr: from key name
        String variant_uuid = (String) variantMap.remove("jcr:uuid");
        // TODO: remove uuid when there are no more consumers for it (Solr, Hydra, Customer Portal)
        variantMap.put("uuid", variant_uuid);
        variantMap.put("variant_uuid", variant_uuid);
        variantMap.put("document_uuid", mv.getParentLocale().getParent().uuid().get());
        // Convert date string to UTC
        Date dateModified = new Date(mv.getResourceMetadata().getModificationTime());
        variantMap.put("date_modified", dateModified.toInstant().toString());
        // Return the body content of the module ONLY
        variantMap.put("body",
                Html.parse(Charsets.UTF_8.name())
                        .andThen(Html.rewriteUuidUrls(request.getResourceResolver(), new CustomerPortalUrlUuidProvider()))
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
        String versionUrlFragment = "";
        String productUrlFragment = "";
        if (pv != null) {
            Map<String, String> productMap = new HashMap<>();
            productList.add(productMap);
            productMap.put("product_version", pv.name().get());
            versionUrlFragment = pv.getValueMap().containsKey("urlFragment") ? pv.urlFragment().get() : "";
            productMap.put("version_url_fragment", versionUrlFragment);
            productUrlFragment = pv.getProduct().getValueMap().containsKey("urlFragment") ? pv.getProduct().urlFragment().get() : "";
            productMap.put("product_name", pv.getProduct().name().get());
            productMap.put("product_url_fragment", productUrlFragment);
        }

        // Process url_fragment from metadata
        String urlFragment = releasedMetadata.get().urlFragment().get() != null ? releasedMetadata.get().urlFragment().get() : "";
        if (!urlFragment.isEmpty()) {
            variantMap.put("vanity_url_fragment", urlFragment);
        } else {
            variantMap.put("vanity_url_fragment", "");
        }

        String searchKeywords = releasedMetadata.get().searchKeywords().get();
        if (searchKeywords != null && !searchKeywords.isEmpty()) {
            variantMap.put("search_keywords", searchKeywords.split(", *"));
        } else {
            variantMap.put("search_keywords", new String[]{});
        }

        // Process view_uri
        if (System.getenv("portal_url") != null) {
            String view_uri = new CustomerPortalUrlUuidProvider().generateUrlString(mv);
            variantMap.put("view_uri", view_uri);
        } else {
            variantMap.put("view_uri", "");
        }
        List<HashMap<String, String>> includeAssemblies = new ArrayList<>();

        //get the assemblies and iterate over them

        ServletHelper.addAssemblyDetails(ServletHelper.getModuleUuidFromVariant(mv), includeAssemblies, request, false, false);
        variantMap.put("included_in_guides", includeAssemblies);
        variantMap.put("isPartOf", includeAssemblies);
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
}
