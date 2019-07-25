package com.redhat.pantheon.servlet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.redhat.pantheon.asciidoctor.AsciidoctorPool;
import com.redhat.pantheon.asciidoctor.extension.MetadataExtractorTreeProcessor;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.Module;
import com.redhat.pantheon.model.ModuleRevision;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.servlets.post.*;
import org.asciidoctor.Asciidoctor;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Streams.stream;

/**
 * Post operation to add a new Module revision to the system.
 * Only thre parameters are expected in the post request:
 * 1. locale - Optional; indicates the locale that the module content is in
 * 2. :operation - This value must be 'pant:newModuleRevision'
 * 3. asciidoc - The file upload (multipart) containing the asciidoc content file for the new module revision.
 *
 * The url to POST a request to the server is the path of the new or existing module to host the content.
 * If there is no content for said url, the module is created and a single revision along with it.
 *
 * @author Carlos Munoz
 */
@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet POST operation which accepts module uploads and versions them appropriately",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:newModuleRevision"
        })
public class ModuleRevisionUpload extends AbstractPostOperation {

    private static final Logger log = LoggerFactory.getLogger(ModuleRevisionUpload.class);

    private AsciidoctorPool asciidoctorPool;

    @Activate
    public ModuleRevisionUpload(@Reference AsciidoctorPool asciidoctorPool) {
        this.asciidoctorPool = asciidoctorPool;
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {

        try {
            String locale = ServletUtils.paramValue(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE.toString());
            String asciidocContent = ServletUtils.paramValue(request, "asciidoc");
            String path = request.getResource().getPath();
            String moduleName = ResourceUtil.getName(path);
            String description = ServletUtils.paramValue(request, "jcr:description");

            log.debug("Pushing new module revision at: " + path + " with locale: " + locale);
            log.trace("and content: " + asciidocContent);

            // Try to find the module
            Resource moduleResource = request.getResourceResolver().getResource(path);

            if(moduleResource == null) {
                Map<String, Object> props = Maps.newHashMap();
                props.put("jcr:primaryType", "pant:module");
                props.put("jcr:title", moduleName);
                props.put("jcr:description", description);


                moduleResource =
                        ResourceUtil.getOrCreateResource(
                                request.getResourceResolver(),
                                path,
                                props,
                                null,
                                false);
            }

            Module module = new Module(moduleResource);

            // get the latest revision
            Module.Revisions revisions = module.locales.getOrCreate()
                    .getOrCreateModuleLocale(LocaleUtils.toLocale(locale))
                    .revisions.getOrCreate();

            // If the revision is empty, then create the new node
            if(!revisions.hasChildren()) {
                createNewRevision(revisions, "v1", moduleName, asciidocContent);
                response.setStatus(HttpServletResponse.SC_CREATED, "New revision created");
            }
            else {
                // if the content matches the latest revision, don't do a thing
                ModuleRevision latestVersion = revisions.getLatestRevision();
                String storedAdocContet = latestVersion
                        .asciidoc.get()
                        .jcrContent.get()
                        .jcrData.get();
                if(latestVersion != null && storedAdocContet != null && storedAdocContet.equals(asciidocContent)) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT, "Nothing has changed");
                }
                // create a new revision
                else {
                    String revisionName = "v" + (stream(revisions.getChildren()).collect(Collectors.counting()) + 1);
                    createNewRevision(revisions, revisionName, moduleName, asciidocContent);
                    response.setStatus(HttpServletResponse.SC_CREATED, "New revision created");
                }
            }

            request.getResourceResolver().commit();

        } catch (Exception e) {
            throw new RepositoryException("Error uploading a module revision", e);
        }
    }

    private ModuleRevision createNewRevision(Module.Revisions revisions, String name, String title, String asciidocContent) {
        ModuleRevision newRevision = revisions.getOrCreateModuleRevision(name);
        // TODO these should be extracted from the content
        newRevision.title.set(title);
        newRevision.description.set("");
        FileResource asciidoc = newRevision.asciidoc.getOrCreate();
        asciidoc.jcrContent.getOrCreate().jcrData.set(asciidocContent);
        asciidoc.jcrContent.getOrCreate().mimeType.set("text/x-asciidoc");
        extractMetadata(newRevision);
        return newRevision;
    }

    private void extractMetadata(ModuleRevision moduleRevision) {
        log.info("=== Start extracting metadata ");
        long startTime = System.currentTimeMillis();
        Asciidoctor asciidoctor = asciidoctorPool.borrowObject();
        try {
            asciidoctor.javaExtensionRegistry().treeprocessor(
                    new MetadataExtractorTreeProcessor(moduleRevision));

            asciidoctor.load(moduleRevision.asciidocContent.get(), newHashMap());
        }
        finally {
            asciidoctorPool.returnObject(asciidoctor);
        }
        long endTime = System.currentTimeMillis();
        log.info("=== End extracting metadata. Time it took: " + (endTime-startTime)/1000 + " secs");
    }

}
