package com.redhat.pantheon.servlet.assembly;

import com.redhat.pantheon.model.assembly.AssemblyContent;
import com.redhat.pantheon.model.assembly.AssemblyPage;
import com.redhat.pantheon.model.assembly.AssemblyVariant;
import com.redhat.pantheon.model.document.DocumentMetadata;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.model.module.ModuleVersion;
import com.redhat.pantheon.servlet.AbstractJsonSingleQueryServlet;
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

/**
 * Get operation to render documents included assembly list in JSON format.
 * Only one parameter is expected in the Get request:
 * 1. variantUuid - required; indicates the uuid string which uniquely identified an assembly variant
 *
 * The url to GET a request from the server is /pantheon/internal/assembly/includes
 * Example: <server_url>/pantheon/internal/assembly/includes/<uuid>
 *
 * @author Lisa Davidson
 */
@Component(
        service = Servlet.class,
        property = {
        Constants.SERVICE_DESCRIPTION + "=Servlet to facilitate GET operation which accepts variant uuid to output documents included in an assembly",
        Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
// /pantheon/internal/assembly/includes.json/${variantUuid}
@SlingServletPaths(value = "/pantheon/internal/assembly/includes")
public class DocumentIncludedServlet extends AbstractJsonSingleQueryServlet {
    public static final String PORTAL_URL = "PORTAL_URL";
    public static final String PANTHEON_HOST = "PANTHEON_HOST";
    public static final String MODULE_VARIANT_API_PATH = "/api/module/variant.json";
    public static final String VARIANT_URL = "url";
    private final Logger log = LoggerFactory.getLogger(DocumentIncludedServlet.class);

    private final SlingPathSuffix suffix = new SlingPathSuffix("/{variantUuid}");

    @Override
    protected String getQuery(SlingHttpServletRequest request) {
        // Get the query parameter(s)
        Map<String, String> parameters = suffix.getParameters(request);
        String uuid = parameters.get("variantUuid");
        StringBuilder query = new StringBuilder("select * from [pant:assemblyVariant] as assemblyVariant WHERE assemblyVariant.[jcr:uuid] = '")
                .append(uuid)
                .append("'");
        return query.toString();
    }

    @Override
    protected boolean isValidResource(@Nonnull SlingHttpServletRequest request, @Nonnull Resource resource) {
        AssemblyVariant assemblyVariant = resource.adaptTo(AssemblyVariant.class);

        if ( assemblyVariant.hasDraft() || assemblyVariant.released().isPresent()) {
            return true;
        }
        setCustomErrorMessage("Assembly version not found for provided variant uuid " + suffix.getParameters(request).get("variantUuid"));
        return false;
    }

    @Override
    protected Map<String, Object> resourceToMap(@Nonnull SlingHttpServletRequest request,
                                                @NotNull Resource resource) throws RepositoryException {
        AssemblyVariant assemblyVariant = resource.adaptTo(AssemblyVariant.class);

        Map<String, Object> variantMap = super.resourceToMap(request, resource);
        Map<String, Object> documentIncluded = new HashMap<>();

        LinkedHashMap<Integer, Object> documents = new LinkedHashMap<>();
        variantMap.put("documents", documents);

        AssemblyContent assemblyContent = assemblyVariant.released().get().content().get();

        if (assemblyContent != null & assemblyContent.getChildren() != null) {
            for (Resource childResource : assemblyContent.getChildren()) {
                AssemblyPage page = childResource.adaptTo(AssemblyPage.class);
                Map<String, String> documentMap = new HashMap<>();
                documents.put(documents.size(), documentMap);
                String moduleUuid = page.module().get();
                Module module = getResourceByUuid(request, moduleUuid).adaptTo(Module.class);
                ModuleVariant canonical = module
                        .locale(assemblyVariant.getParentLocale().getName()).get()
                        .variants().get()
                        .canonicalVariant().get();
                documentMap.put("canonical_uuid", canonical.uuid().get());
                documentMap.put("variant_path", canonical.getPath());
                documentMap.put("title", getModuleTitleFromUuid(canonical));
            }
            // Show number of documents included
            variantMap.put("document_count", documents.size());
        }

        // remove unnecessary fields from the map
        variantMap.remove("jcr:lastModified");
        variantMap.remove("jcr:lastModifiedBy");
        variantMap.remove("jcr:createdBy");
        variantMap.remove("jcr:created");
        variantMap.remove("sling:resourceType");
        variantMap.remove("jcr:primaryType");
        variantMap.remove("jcr:uuid");
        // Adding variantMap to a parent documentIncluded map
        documentIncluded.put("includes", variantMap);

        return documentIncluded;
    }

    private String getModuleTitleFromUuid(ModuleVariant moduleVariant) {
        String moduleTitle;
        if (moduleVariant.hasDraft()) {
            moduleTitle = moduleVariant.draft()
                    .traverse()
                    .toChild(ModuleVersion::metadata)
                    .toField(DocumentMetadata::title)
                    .get();
        } else if (moduleVariant.released().isPresent()) {
            moduleTitle = moduleVariant.released()
                    .traverse()
                    .toChild(ModuleVersion::metadata)
                    .toField(DocumentMetadata::title)
                    .get();
        } else {
            moduleTitle = "";
        }

        return moduleTitle;
    }
}
