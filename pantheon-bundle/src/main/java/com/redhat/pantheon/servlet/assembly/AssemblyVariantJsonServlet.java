package com.redhat.pantheon.servlet.assembly;

import com.google.common.base.Charsets;
import com.ibm.icu.util.ULocale;
import com.redhat.pantheon.extension.url.CustomerPortalUrlUuidProvider;
import com.redhat.pantheon.extension.url.DrupalXrefProvider;
import com.redhat.pantheon.html.Html;
import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.assembly.*;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.servlet.AbstractJsonSingleQueryServlet;
import com.redhat.pantheon.servlet.ServletUtils;
import com.redhat.pantheon.servlet.util.ServletHelper;
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

import static com.redhat.pantheon.servlet.util.ServletHelper.getResourceByUuid;
import static javax.servlet.http.HttpServletResponse.SC_OK;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet to facilitate GET operation which accepts assembly variant uuid to output assembly data",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
// /api/assembly/variant.json/${variantUuid}";
@SlingServletPaths(value = "/api/assembly/variant")
public class AssemblyVariantJsonServlet extends AbstractJsonSingleQueryServlet {
    public static final String PRODUCT_VERSION = "product_version";
    public static final String VERSION_URL_FRAGMENT = "version_url_fragment";
    public static final String PRODUCT_NAME = "product_name";
    public static final String PRODUCT_URL_FRAGMENT = "product_url_fragment";
    public static final String VANITY_URL_FRAGMENT = "vanity_url_fragment";
    public static final String SEARCH_KEYWORDS = "search_keywords";
    public static final String VIEW_URI = "view_uri";
    public static final String PORTAL_URL = "PORTAL_URL";
    public static final String PANTHEON_HOST = "PANTHEON_HOST";
    public static final String MODULE_VARIANT_API_PATH = "/api/module/variant.json";
    public static final String VARIANT_URL = "url";
    public static final String PANTHEON_ENV = "PANTHEON_ENV";

    private final Logger log = LoggerFactory.getLogger(AssemblyVariantJsonServlet.class);

    private final SlingPathSuffix suffix = new SlingPathSuffix("/{variantUuid}");

    @Override
    protected String getQuery(SlingHttpServletRequest request) {
        // Get the query parameter(s)
        Map<String, String> parameters = suffix.getParameters(request);
        String uuid = parameters.get("variantUuid");
        // Hydra fetch calls look like this:
        // Calling pantheon2 with url https://<HOST>/api/assembly/variant.json/b537ef3c-5c7d-4280-91ce-e7e818e6cc11&proxyHost=<SOMEHOST>&proxyPort=8080&throwExceptionOnFailure=false
        StringBuilder query = new StringBuilder("select * from [pant:assemblyVariant] as assemblyVariant WHERE assemblyVariant.[jcr:uuid] = '")
                .append(sanitizeSuffix(uuid))
                .append("'");
        return query.toString();
    }

    @Override
    protected boolean isValidResource(@Nonnull SlingHttpServletRequest request, @Nonnull Resource resource) {
        AssemblyVariant assemblyVariant = resource.adaptTo(AssemblyVariant.class);
        Optional<AssemblyVersion> releasedRevision = assemblyVariant != null ? Optional.ofNullable(assemblyVariant.released().get()) : Optional.empty();
        if (!releasedRevision.isPresent()) {
            setCustomErrorMessage("Released assembly version not found for provided variant uuid " + suffix.getParameters(request).get("variantUuid"));
        }
        return releasedRevision.isPresent();
    }

    @Override
    protected Map<String, Object> resourceToMap(@Nonnull SlingHttpServletRequest request,
                                                @NotNull Resource resource) throws RepositoryException {
        AssemblyVariant assemblyVariant = resource.adaptTo(AssemblyVariant.class);
        Optional<AssemblyMetadata> releasedMetadata = Child.from(assemblyVariant)
                .toChild(AssemblyVariant::released)
                .toChild(AssemblyVersion::metadata)
                .asOptional();
        Optional<FileResource> releasedContent = Child.from(assemblyVariant)
                .toChild(AssemblyVariant::released)
                .toChild(AssemblyVersion::cachedHtml)
                .asOptional();
        Optional<AssemblyVersion> releasedRevision = Child.from(assemblyVariant)
                .toChild(AssemblyVariant::released)
                .asOptional();

        Map<String, Object> variantMap = super.resourceToMap(request, resource);
        Map<String, Object> variantDetails = new HashMap<>();

        variantDetails.put("status", SC_OK);
        variantDetails.put("message", "Assembly Found");

        String resourcePath = resource.getPath();
        Locale locale = ULocale.createCanonical(assemblyVariant.getParentLocale().getName()).toLocale();
        variantMap.put("locale", ServletUtils.toLanguageTag(locale));
        variantMap.put("revision_id", releasedRevision.get().getName());
        variantMap.put("title", releasedMetadata.get().title().get());
        variantMap.put("headline", releasedMetadata.get().getValueMap().containsKey("pant:headline") ? releasedMetadata.get().headline().get() : "");
        variantMap.put("description", releasedMetadata.get().mAbstract().get());
        variantMap.put("content_type", "assembly");
        variantMap.put("date_published", releasedMetadata.get().getValueMap().containsKey("pant:datePublished") ? releasedMetadata.get().datePublished().get().toInstant().toString() : "");
        variantMap.put("date_first_published", releasedMetadata.get().getValueMap().containsKey("pant:dateFirstPublished") ? releasedMetadata.get().dateFirstPublished().get().toInstant().toString() : "");
        variantMap.put("status", "published");

        // Assume the path is something like: /content/<something>/my/resource/path
        variantMap.put("assembly_url_fragment", resourcePath.substring("/content/repositories/".length()));

        // Striping out the jcr: from key name
        String variant_uuid = (String) variantMap.remove("jcr:uuid");
        // TODO: remove uuid when there are no more consumers for it (Solr, Hydra, Customer Portal)
        variantMap.put("uuid", variant_uuid);
        variantMap.put("variant_uuid", variant_uuid);
        variantMap.put("document_uuid", assemblyVariant.getParentLocale().getParent().uuid().get());
        // Convert date string to UTC
        Date dateModified = new Date(resource.getResourceMetadata().getModificationTime());
        variantMap.put("date_modified", dateModified.toInstant().toString());
        // Return the body content of the assembly ONLY
        variantMap.put("body",
                Html.parse(Charsets.UTF_8.name())
                        .andThen(Html.rewriteUuidUrls(request.getResourceResolver(), new DrupalXrefProvider()))
                        .andThen(Html.getElementById("doc-content", Html.getElementByTagName("cp-documentation", Html.getBody())))
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
        String productUrlFragment = "";
        String versionUrlFragment = "";
        if (pv != null) {
            Map<String, String> productMap = new HashMap<>();
            productList.add(productMap);
            productMap.put(PRODUCT_VERSION, pv.name().get());
            versionUrlFragment = pv.getValueMap().containsKey("urlFragment") ? pv.urlFragment().get() : "";
            productMap.put(VERSION_URL_FRAGMENT, versionUrlFragment);
            productUrlFragment = pv.getProduct().getValueMap().containsKey("urlFragment") ? pv.getProduct().urlFragment().get() : "";
            productMap.put(PRODUCT_NAME, pv.getProduct().name().get());
            productMap.put(PRODUCT_URL_FRAGMENT, productUrlFragment);
        }

        // Process url_fragment from metadata
        String urlFragment = releasedMetadata.get().urlFragment().get() != null ? releasedMetadata.get().urlFragment().get() : "";
        if (!urlFragment.isEmpty()) {
            variantMap.put(VANITY_URL_FRAGMENT, urlFragment);
        } else {
            variantMap.put(VANITY_URL_FRAGMENT, "");
        }

        String searchKeywords = releasedMetadata.get().searchKeywords().get();
        if (searchKeywords != null && !searchKeywords.isEmpty()) {
            variantMap.put(SEARCH_KEYWORDS, searchKeywords.split(", *"));
        } else {
            variantMap.put(SEARCH_KEYWORDS, new String[]{});
        }

        // Process view_uri
        if (System.getenv(PORTAL_URL) != null) {
            String view_uri = new CustomerPortalUrlUuidProvider().generateUrlString(assemblyVariant);
            variantMap.put(VIEW_URI, view_uri);
        } else {
            variantMap.put(VIEW_URI, "");
        }

        List<Map<String, String>> moduleList = new ArrayList<>();
        List<Map<String, String>> publishedModuleList = new ArrayList<>();
        variantMap.put("modules_included", moduleList);

        AssemblyContent assemblyContent = assemblyVariant.released().get().content().get();

        if (assemblyContent != null & assemblyContent.getChildren() != null) {
            for (Resource childResource : assemblyContent.getChildren()) {
                AssemblyPage page = childResource.adaptTo(AssemblyPage.class);
                Map<String, String> moduleMap = new HashMap<>();
                moduleList.add(moduleMap);

                String moduleUuid = page.module().get();
                Module module = getResourceByUuid(request, moduleUuid).adaptTo(Module.class);
                ModuleVariant canonical = module
                        .locale(assemblyVariant.getParentLocale().getName()).get()
                        .variants().get()
                        .canonicalVariant().get();
                moduleMap.put("canonical_uuid", canonical.uuid().get());
                // Get the current title from module instead of using the stored value in the content node
                // When module title is modified, the value stored in content node becomes stale.
                // Sometime, the title stored in content node shows filename instead of title.
                moduleMap.put("title", ServletHelper.getModuleTitleFromUuid(canonical));
                moduleMap.put("module_uuid", module.uuid().get());
                // check if the module is published
                if (canonical.released().isPresent() && System.getenv(PANTHEON_HOST) != null) {
                    String variantUrl = System.getenv(PANTHEON_HOST)
                            + MODULE_VARIANT_API_PATH
                            + "/"
                            + canonical.uuid().get();
                    String relativeUrl = MODULE_VARIANT_API_PATH + "/" + canonical.uuid().get();
                    moduleMap.put(VARIANT_URL, variantUrl);
                    moduleMap.put("relative_url", relativeUrl);
                } else {
                    moduleMap.put(VARIANT_URL, "");
                    moduleMap.put("relative_url", "");
                }
                moduleMap.put("level_offset", String.valueOf(page.leveloffset().get()));
                if (canonical.released().isPresent() && System.getenv(PANTHEON_ENV) != null) {}
                    moduleMap.put("pantheon_env", System.getenv(PANTHEON_ENV));
            }
            moduleList.stream().filter(map -> map.containsKey(VARIANT_URL))
                    .filter(map -> !map.get(VARIANT_URL).isEmpty())
                    .forEach(publishedModuleList::add);
            variantMap.put("hasPart", publishedModuleList);
        }
        // remove unnecessary fields from the map
        variantMap.remove("jcr:lastModified");
        variantMap.remove("jcr:lastModifiedBy");
        variantMap.remove("jcr:createdBy");
        variantMap.remove("jcr:created");
        variantMap.remove("sling:resourceType");
        variantMap.remove("jcr:primaryType");

        // Adding variantMap to a parent variantDetails map
        variantDetails.put("assembly", variantMap);

        return variantDetails;
    }

    private String sanitizeSuffix(String suffix) {
        // b537ef3c-5c7d-4280-91ce-e7e818e6cc11&proxyHost=<SOMEHOST>&proxyPort=8080&throwExceptionOnFailure=false

        if (suffix.contains("&")) {
            String[] parts = suffix.split("\\&");
            suffix = parts[0];
        }

        if (suffix.contains("?")) {
            String[] parts = suffix.split("\\?");
            suffix = parts[0];
        }

        return suffix;
    }

    private String getModuleUuidFromVariant(ModuleVariant moduleVariant) {
        Resource resource = moduleVariant.getParentLocale().getParent();
        Module module = resource.adaptTo(Module.class);
        return module.uuid().get();
    }
}
