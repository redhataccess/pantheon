package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.FileResource.JcrContent;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.Content;
import com.redhat.pantheon.model.module.AckStatus;
import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVersion;
import com.redhat.pantheon.model.module.ModuleType;
import com.redhat.pantheon.servlet.ServletUtils;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.commons.lang3.LocaleUtils;
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
 * Only thre parameters are expected in the post request:
 * 1. locale - Optional; indicates the locale that the module content is in
 * 2. :operation - This value must be 'pant:newModuleVersion'
 * 3. asciidoc - The file upload (multipart) containing the asciidoc content file for the new module version.
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
    private static final Set<String> EXCLUDES = Collections.unmodifiableSet(
            new HashSet<>(
                    Arrays.asList(
                            "jcr:description",
                            "jcr:lastModified",
                            "jcr:primaryType",
                            "jcr:title",
                            "pant:dateUploaded",
                            "pant:datePublished"
                    )));

    private AsciidoctorService asciidoctorService;
    private ServiceResourceResolverProvider serviceResourceResolverProvider;

    @Activate
    public ModuleVersionUpload(
            @Reference AsciidoctorService asciidoctorService,
            @Reference ServiceResourceResolverProvider serviceResourceResolverProvider) {
        this.asciidoctorService = asciidoctorService;
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {

        try {
            String locale = ServletUtils.paramValue(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE.toString());
            String asciidocContent = ServletUtils.paramValue(request, "asciidoc");
            String encoding = request.getCharacterEncoding();
            if (encoding != null) {
                asciidocContent = new String(asciidocContent.getBytes(encoding), StandardCharsets.UTF_8);
            }
            String path = request.getResource().getPath();
            String moduleName = ResourceUtil.getName(path);
            String description = ServletUtils.paramValue(request, "jcr:description", "");

            log.debug("Pushing new module version at: " + path + " with locale: " + locale);
            log.trace("and content: " + asciidocContent);
            int responseCode = HttpServletResponse.SC_OK;

            // Try to find the module
            ResourceResolver resolver = request.getResourceResolver();
            Resource moduleResource = resolver.getResource(path);
            Module module;

            if(moduleResource == null) {
                module =
                        SlingModels.createModel(
                                resolver,
                                path,
                                Module.class);
                responseCode = HttpServletResponse.SC_CREATED;
            } else {
                module = moduleResource.adaptTo(Module.class);
            }

            Locale localeObj = LocaleUtils.toLocale(locale);
            Optional<ModuleVersion> draftVersion = module.getDraftVersion(localeObj);
            // if there is no draft content, create it
            if( !draftVersion.isPresent() ) {
                draftVersion = Optional.of(
                        module.getOrCreateModuleLocale(localeObj)
                        .createNextVersion());
                module.getOrCreateModuleLocale(localeObj)
                        .draft().set( draftVersion.get().uuid().get() );
                //Need to copy the metadata from the released version, if it exists
                Optional<ModuleVersion> releasedVersion = module.getReleasedVersion(localeObj);
                if (releasedVersion.isPresent()) {
                    Metadata releasedMeta = releasedVersion.get().metadata().get();
                    Metadata draftMeta = draftVersion.get().metadata().getOrCreate();

                    for (Map.Entry<String, Object> e : releasedMeta.getValueMap().entrySet()) {
                        if (!EXCLUDES.contains(e.getKey())) {
                            draftMeta.setProperty(e.getKey(), e.getValue());
                        }
                    }
                }
            }

            // modify only the draft content/metadata
            JcrContent jcrContent = draftVersion.get()
                    .content().getOrCreate()
                    .asciidoc().getOrCreate()
                    .jcrContent().getOrCreate();
            boolean generateHtml = false;
            String jcrData = jcrContent.jcrData().get();

            // Html is generated if:
            // a. the draft content has changed as part of this upload
            // b. a draft hasn't already been built before
            if ((jcrData != null && !jcrData.equals(asciidocContent))
                    || !draftVersion.map(ModuleVersion::content)
                            .map(Supplier::get)
                            .map(Content::cachedHtml)
                            .map(Supplier::get)
                            .isPresent()) {
                generateHtml = true;
            }
            jcrContent.jcrData().set(asciidocContent);
            jcrContent.mimeType().set("text/x-asciidoc");

            Metadata metadata = draftVersion.get()
                    .metadata().getOrCreate();
            
            if(metadata.title().get()==null){
                metadata.title().set(moduleName);
            }                    
            metadata.description().set(description);
            Calendar now = Calendar.getInstance();
            metadata.dateModified().set(now);
            metadata.dateUploaded().set(now);

            AckStatus status = draftVersion.get()
                .ackStatus().getOrCreate();
            status.dateModified().set(now);
            resolver.commit();

            if (generateHtml) {
                Map<String, Object> context = asciidoctorService.buildContextFromRequest(request);
                // drop the html on the floor, this is just to cache the results
                asciidoctorService.getModuleHtml(draftVersion.get(), module, context, true);
            }

            // Generate a module type based on the file name ONLY after asciidoc generation, so that the
            // attribute-based logic takes precedence
            if(metadata.moduleType().get() == null) {
                metadata.moduleType().set(determineModuleType(module));
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
}
