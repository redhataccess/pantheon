package com.redhat.pantheon.servlet.module;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.FileResource.JcrContent;
import com.redhat.pantheon.model.api.Folder;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.Content;
import com.redhat.pantheon.model.module.AckStatus;
import com.redhat.pantheon.model.module.HashableFileResource;
import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleLocale;
import com.redhat.pantheon.model.module.ModuleVersion;
import com.redhat.pantheon.model.module.ModuleType;
import com.redhat.pantheon.model.workspace.Workspace;
import com.redhat.pantheon.servlet.ServletUtils;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.servlets.post.AbstractPostOperation;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.PostOperation;
import org.apache.sling.servlets.post.PostResponse;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Post operation to add a new Module version to the system.
 * The expected parameters in the post request are:
 * 1. locale - Optional; indicates the locale that the module content is in
 * 2. :operation - This value must be 'pant:newModuleVersion'
 * 3. asciidoc - The file upload (multipart) containing the asciidoc content file for the new module version.
 * 4. ws - The name of the workspace where to place the uploaded module
 *
 * The url to POST a request to the server is the path of the new or existing module to host the content.
 * If there is no content for said url, the module is created and a single version along with it.
 *
 * @author Carlos Munoz
 */
@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet POST operation which accepts module uploads and versions them appropriately",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:newModuleVersion"
        })
public class ModuleVersionUpload extends AbstractPostOperation {

    private static final Logger log = LoggerFactory.getLogger(ModuleVersionUpload.class);

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {

        try {
            String locale = ServletUtils.paramValue(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE.toString());
            String asciidocContent = ServletUtils.paramValue(request, "asciidoc");
            String ws = ServletUtils.paramValue(request, "ws");

            if( ws == null ) {
                // TODO This will return a 500. Find a way to return something nicer
                throw new ServletException("Workspace parameter must be provided");
            }

            String encoding = request.getCharacterEncoding();
            if (encoding != null) {
                asciidocContent = new String(asciidocContent.getBytes(encoding), StandardCharsets.UTF_8);
            }

            // This should be the path relative to the root of the git file system
            String path = request.getResource().getPath();
            String moduleName = ResourceUtil.getName(path);

            log.debug("Pushing new module version at: " + path + " with locale: " + locale);
            log.trace("and content: " + asciidocContent);
            int responseCode = HttpServletResponse.SC_OK;

            // Try to find the module
            ResourceResolver resolver = request.getResourceResolver();

            /*
             * /path/to/module.adoc
             * /content/repositories/<<ws>>/entities/path/to/module.adoc
             */
            Workspace workspace = SlingModels.getModel(resolver, "/content/repositories/" + ws, Workspace.class);
            Folder moduleParent = workspace.entities().getOrCreate()
                    .createSubPath(PathUtils.getParentPath(path));

            Module module = SlingModels.getModel(resolver, PathUtils.concat(moduleParent.getPath(), moduleName), Module.class);

            if(module == null) {
                module =
                    SlingModels.createModel(moduleParent, PathUtils.getName(path), Module.class);
                responseCode = HttpServletResponse.SC_CREATED;
            }

            Locale localeObj = LocaleUtils.toLocale(locale);
            ModuleLocale moduleLocale = module.getOrCreateModuleLocale(localeObj);
            HashableFileResource draftSrc = moduleLocale
                    .source().getOrCreate()
                    .draft().getOrCreate();

            // Check if the content is the same as what is hashed already
            HashCode incomingSrcHash = hash(asciidocContent);
            String storedSrcHash = draftSrc.hash().get();
            // If the source content is the same, don't update it
            if(incomingSrcHash.toString().equals( storedSrcHash )) {
                responseCode = HttpServletResponse.SC_NOT_MODIFIED;
            } else {
                draftSrc.jcrContent().getOrCreate()
                        .jcrData().set(asciidocContent);
                draftSrc.jcrContent().getOrCreate()
                        .mimeType().set("text/x-asciidoc");

                // TODO Html can no longer be generated on upload since there might be too many variants
                // TODO This will need to be re-thought since metadata now lies on the variant
                // Generate a module type based on the file name ONLY after asciidoc generation, so that the
                // attribute-based logic takes precedence
                // if(metadata.moduleType().get() == null) {
                //    metadata.moduleType().set(determineModuleType(module));
                //}
            }

            resolver.commit();
            response.setStatus(responseCode, "");
        } catch (Exception e) {
            throw new RepositoryException("Error uploading a module version", e);
        }
    }

    /**
     * Determines the module type from the uploaded module version
     * @param module The uploaded module
     * @return A module type for the module version, or null if one cannot be determined.
     */
    private static ModuleType determineModuleType(Module module) {
        String fileName = module.getName();

        if( fileName.startsWith("proc_") ) {
            return ModuleType.PROCEDURE;
        }
        else if( fileName.startsWith("con_") ) {
            return ModuleType.CONCEPT;
        }
        else if( fileName.startsWith("ref_") ) {
            return ModuleType.REFERENCE;
        }
        else {
            return null;
        }
    }

    /*
     * calculates a hash for a string
     * TODO This should probably be moved elsewhere
     */
    private HashCode hash(String str) {
        return Hashing.adler32().hashString(str == null ? "" : str, Charsets.UTF_8);
    }
}
