package com.redhat.pantheon.servlet.util;


import com.redhat.pantheon.extension.url.CustomerPortalUrlUuidProvider;
import com.redhat.pantheon.extension.url.UrlException;
import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.ModelException;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.assembly.AssemblyMetadata;
import com.redhat.pantheon.model.assembly.AssemblyVariant;
import com.redhat.pantheon.model.assembly.AssemblyVersion;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentMetadata;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.model.module.ModuleVersion;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The type Servlet helper.
 */
public class ServletHelper {
    /**
     * The constant PANTHEON_HOST.
     */
    public static final String PANTHEON_HOST = "PANTHEON_HOST";
    /**
     * The constant PORTAL_URL.
     */
    public static final String PORTAL_URL = "PORTAL_URL";
    /**
     * The constant ASSEMBLY_VARIANT_API_PATH.
     */
    public static final String ASSEMBLY_VARIANT_API_PATH = "/api/assembly/variant.json";
    /**
     * The constant PANTHEON_ENV.
     */
    public static final String PANTHEON_ENV = "PANTHEON_ENV";

    private static final Set<String> DOCUMENT_TYPES = new HashSet<>();
    private static final Set<String> VARIANT_TYPES = new HashSet<>();
    static {
        DOCUMENT_TYPES.add(PantheonConstants.RESOURCE_TYPE_ASSEMBLY);
        DOCUMENT_TYPES.add(PantheonConstants.RESOURCE_TYPE_MODULE);
        VARIANT_TYPES.add(PantheonConstants.RESOURCE_TYPE_ASSEMBLYVARIANT);
        VARIANT_TYPES.add(PantheonConstants.RESOURCE_TYPE_MODULEVARIANT);
    }

    private static final Logger LOG = LoggerFactory.getLogger(ServletHelper.class);

    /**
     * Instantiates a new Servlet helper.
     */
    public ServletHelper() {}

    /**
     * Returns a Resource given uuid
     *
     * @param request the request
     * @param uuid    the uuid
     * @return a Resource
     * @throws RepositoryException the repository exception
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

    /**
     * Tranform resource path to model.
     * @param r     a JCR resource
     * @return
     */
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

    /**
     * Add assembly details.
     *
     * @param moduleUuid        the module uuid
     * @param includeAssemblies the container for assembly details
     * @param request           the request
     * @param addPath           whether or not to add the path of assembly to details
     * @param canHaveDraft      can the draft version be included
     * @throws RepositoryException the repository exception
     */
    public static void addAssemblyDetails(String moduleUuid, List<HashMap<String, String>> includeAssemblies,
                                          SlingHttpServletRequest request, boolean addPath, boolean canHaveDraft) throws RepositoryException {
        JcrQueryHelper helper = new JcrQueryHelper(request.getResourceResolver());
        helper.query(getQueryForIncludedInAssemblies(moduleUuid,canHaveDraft)
                ,1000L, 0L, Query.XPATH)
                .forEach(a->setAssemblyData(a,includeAssemblies, addPath, canHaveDraft));
    }

    private static String getQueryForIncludedInAssemblies(String moduleUuid, boolean canHaveDraft) {
        if(!canHaveDraft){
           return  "/jcr:root/content/(repositories | assemblies | variants)//element(*, pant:assemblyVariant)[(released/content/*/@pant:moduleUuid='" + moduleUuid + "')]";
        }
        return "/jcr:root/content/(repositories | assemblies | variants)//element(*, pant:assemblyVariant)[(*/content/*/@pant:moduleUuid='" + moduleUuid + "')]";
    }

    /**
     * Gets module uuid from variant.
     *
     * @param moduleVariant the module variant
     * @return the module uuid from variant
     */
    public static String getModuleUuidFromVariant(ModuleVariant moduleVariant) {
         return moduleVariant
                 .getParentLocale()
                 .getParent()
                 .uuid()
                 .get();
    }

    /**
     * Sets assembly data.
     *
     * @param resource          the resource representing an AssemblyVariant
     * @param includeAssemblies the container for assembly details
     * @param addPath           whether or not to add the path of assembly to details
     * @param canHaveDraft      can the draft version be included
     */
    public static void setAssemblyData(Resource resource, List<HashMap<String, String>> includeAssemblies, boolean addPath, boolean canHaveDraft) {
        AssemblyVariant assemblyVariant = resource.adaptTo(AssemblyVariant.class);
        HashMap<String,String> assemblyVariantDetails = new HashMap<>();
        // if draft version cannot be added, however only draft exists, then just return
        if(!canHaveDraft&&assemblyVariant.hasDraft()&&!assemblyVariant.released().isPresent()){
            return;
        }
        Optional<AssemblyMetadata> metadata = Child.from(assemblyVariant)
                .toChild(assemblyVariant.hasDraft()&&canHaveDraft?AssemblyVariant::draft:AssemblyVariant::released)
                .toChild(AssemblyVersion::metadata)
                .asOptional();
        assemblyVariantDetails.put("uuid", assemblyVariant.uuid().get());
        assemblyVariantDetails.put("title", metadata.get().title().get());
        if(assemblyVariant.released().isPresent()&& System.getenv(PANTHEON_HOST) != null){
            String assemblyUrl = System.getenv(PANTHEON_HOST)
                    + ASSEMBLY_VARIANT_API_PATH
                    + "/"
                    + assemblyVariant.uuid().get();
            String relativeUrl = ASSEMBLY_VARIANT_API_PATH + "/" + assemblyVariant.uuid().get();
            assemblyVariantDetails.put("url", assemblyUrl);
            assemblyVariantDetails.put("relative_url", relativeUrl);
        }
        if (assemblyVariant.released().isPresent() && System.getenv(PORTAL_URL) != null) {
            // Add Customer Portal view_uri
            try {
                String view_uri = new CustomerPortalUrlUuidProvider(assemblyVariant).generateUrlString();
                assemblyVariantDetails.put("view_uri", view_uri);
            } catch (UrlException e) {
                LOG.warn("Failed to generate URL for " + assemblyVariant.getPath() + " but could not.", e);
            }
        }
        if(addPath){
            assemblyVariantDetails.put("path", assemblyVariant.getParentLocale().getParent().getPath());
        }
        if (assemblyVariant.released().isPresent() && System.getenv(PANTHEON_ENV) != null) {
            assemblyVariantDetails.put("pantheon_env", System.getenv(PANTHEON_ENV));
        }
        includeAssemblies.add(assemblyVariantDetails);

    }

    /**
     * Sanitize suffix string.
     *
     * @param suffix the suffix
     * @return the string
     */
    public static String sanitizeSuffix( String suffix) {
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

    /**
     * Get Module Title From moduleVariant UUID.
     * @param moduleVariant
     * @return the String
     */
    public static String getModuleTitleFromUuid(ModuleVariant moduleVariant) {
        String moduleTitle;
        if (moduleVariant.hasDraft()) {
            moduleTitle = moduleVariant.draft()
                    .toChild(ModuleVersion::metadata)
                    .toField(DocumentMetadata::title)
                    .get();
        } else if (moduleVariant.released().isPresent()) {
            moduleTitle = moduleVariant.released()
                    .toChild(ModuleVersion::metadata)
                    .toField(DocumentMetadata::title)
                    .get();
        } else {
            moduleTitle = "";
        }

        return moduleTitle;
    }
}
