package com.redhat.pantheon.servlet.util;

import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.assembly.AssemblyMetadata;
import com.redhat.pantheon.model.assembly.AssemblyVariant;
import com.redhat.pantheon.model.assembly.AssemblyVersion;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVariant;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;

/**
 * The type Servlet helper.
 */
public class ServletHelper {
    /**
     * The constant PANTHEON_HOST.
     */
    public static final String PANTHEON_HOST = "PANTHEON_HOST";
    /**
     * The constant ASSEMBLY_VARIANT_API_PATH.
     */
    public static final String ASSEMBLY_VARIANT_API_PATH = "/api/assembly/variant.json";

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

        Optional<AssemblyMetadata> metadata = traverseFrom(assemblyVariant)
                .toChild(canHaveDraft&&assemblyVariant.hasDraft()?AssemblyVariant::draft:AssemblyVariant::released)
                .toChild(AssemblyVersion::metadata)
                .getAsOptional();
        assemblyVariantDetails.put("uuid", assemblyVariant.uuid().get());
        assemblyVariantDetails.put("title", metadata.get().title().get());
        if(assemblyVariant.released().isPresent()&& System.getenv(PANTHEON_HOST) != null){
            String assemblyUrl = System.getenv(PANTHEON_HOST)
                    + ASSEMBLY_VARIANT_API_PATH
                    + "/"
                    + assemblyVariant.uuid().get();
            assemblyVariantDetails.put("url", assemblyUrl);
        }
        if(addPath){
            assemblyVariantDetails.put("path", assemblyVariant.getParentLocale().getParent().getPath());
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
}
