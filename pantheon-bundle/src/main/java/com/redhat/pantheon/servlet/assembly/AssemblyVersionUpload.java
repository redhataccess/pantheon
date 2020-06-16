package com.redhat.pantheon.servlet.assembly;

import com.google.common.hash.HashCode;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.assembly.Assembly;
import com.redhat.pantheon.model.assembly.AssemblyLocale;
import com.redhat.pantheon.model.module.HashableFileResource;
import com.redhat.pantheon.servlet.ServletUtils;
import com.redhat.pantheon.servlet.module.ModuleVersionUpload;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.servlets.post.AbstractPostOperation;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.PostOperation;
import org.apache.sling.servlets.post.PostResponse;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import static com.redhat.pantheon.jcr.JcrResources.hash;

@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet POST operation which accepts module uploads and versions them appropriately",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:newAssemblyVersion"
        })
public class AssemblyVersionUpload extends AbstractPostOperation {
    private static final Logger log = LoggerFactory.getLogger(ModuleVersionUpload.class);

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {

        try {
            String locale = ServletUtils.paramValue(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE.toString());
            String asciidocContent = ServletUtils.paramValue(request, "asciidoc");
//            String contentType = ServletUtils.paramValue(request, "type", "assembly");

            String encoding = request.getCharacterEncoding();
            if (encoding != null) {
                asciidocContent = new String(asciidocContent.getBytes(encoding), StandardCharsets.UTF_8);
            }

            String path = request.getResource().getPath();

            log.debug("Pushing new version at: " + path + " with locale: " + locale);
            log.trace("and content: " + asciidocContent);
            int responseCode = HttpServletResponse.SC_OK;

            // Try to find the resource
            ResourceResolver resolver = request.getResourceResolver();
            Resource resource = resolver.getResource(path);
            // TODO: need make it more generic so that it can create both module and assemly contentTypes
            Assembly assembly;
            if (resource == null) {
                assembly =
                        SlingModels.createModel(
                                resolver,
                                path,
                                Assembly.class);
                responseCode = HttpServletResponse.SC_CREATED;
            } else {
                assembly = resource.adaptTo(Assembly.class);
            }

            Locale localeObj = LocaleUtils.toLocale(locale);
            AssemblyLocale assemblyLocale = assembly.assemblyLocale(localeObj).getOrCreate();
            HashableFileResource draftSrc = assemblyLocale
                    .source().getOrCreate()
                    .draft().getOrCreate();

            // Check if the content is the same as what is hashed already
            HashCode incomingSrcHash = hash(asciidocContent);
            String storedSrcHash = draftSrc.hash().get();
            // If the source content is the same, don't update it
            if (incomingSrcHash.toString().equals(storedSrcHash)) {
                responseCode = HttpServletResponse.SC_NOT_MODIFIED;
            } else {
                draftSrc.jcrContent().getOrCreate()
                        .jcrData().set(asciidocContent);
                draftSrc.jcrContent().getOrCreate()
                        .mimeType().set("text/x-asciidoc");
            }

            resolver.commit();

            // TODO: trigger an event to generate the html asynchronous
            response.setStatus(responseCode, "");
        } catch (Exception e) {
            throw new RepositoryException("Error uploading an assembly version", e);
        }
    }
}
