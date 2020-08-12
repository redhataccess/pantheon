package com.redhat.pantheon.servlet.assembly;

import com.google.common.hash.HashCode;
import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.jcr.JcrResources;
import com.redhat.pantheon.model.HashableFileResource;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.assembly.Assembly;
import com.redhat.pantheon.model.assembly.AssemblyLocale;
import com.redhat.pantheon.model.assembly.AssemblyMetadata;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet POST operation which accepts module uploads and versions them appropriately",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:newAssemblyVersion"
        })
public class AssemblyVersionUpload extends AbstractPostOperation {
    private static final Logger log = LoggerFactory.getLogger(ModuleVersionUpload.class);

    private AsciidoctorService asciidoctorService;

    @Activate
    public AssemblyVersionUpload(
            @Reference AsciidoctorService asciidoctorService) {
        this.asciidoctorService = asciidoctorService;
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {

        try {
            String locale = ServletUtils.paramValue(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE.toString());
//            String contentType = ServletUtils.paramValue(request, "type", "assembly");

            String encoding = request.getCharacterEncoding();
            if (encoding == null) {
                encoding = StandardCharsets.UTF_8.name();
            }

            String path = request.getResource().getPath();

            log.debug("Pushing new version at: " + path + " with locale: " + locale);
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
            AssemblyLocale assemblyLocale = assembly.locale(localeObj).getOrCreate();
            HashableFileResource draftSrc = assemblyLocale
                    .source().getOrCreate()
                    .draft().getOrCreate();

            // Check if the content is the same as what is hashed already
            HashCode incomingSrcHash =
                    ServletUtils.handleParamAsStream(request, "asciidoc",
                            inputStream -> {
                                try {
                                    return JcrResources.hash(inputStream);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
            String storedSrcHash = draftSrc.hash().get();
            // If the source content is the same, don't update it
            if (incomingSrcHash.toString().equals(storedSrcHash)) {
                responseCode = HttpServletResponse.SC_NOT_MODIFIED;
            } else {
                ServletUtils.handleParamAsStream(request, "asciidoc", encoding,
                        inputStream -> {
                            Session session = resolver.adaptTo(Session.class);
                            draftSrc.jcrContent().getOrCreate()
                                    .jcrData().toFieldType(InputStream.class)
                                    .set(inputStream);
                            return null;
                        });
                draftSrc.hash().set( incomingSrcHash.toString() );
                draftSrc.jcrContent().getOrCreate()
                        .mimeType().set("text/x-asciidoc");

                resolver.commit();

                Map<String, Object> context = asciidoctorService.buildContextFromRequest(request);
                asciidoctorService.getModuleHtml(assembly, localeObj, assembly.getWorkspace().getCanonicalVariantName(),
                        true, context, true);

                AssemblyMetadata moduleMetadata = assemblyLocale
                        .variants().getOrCreate()
                        .variant(
                                assemblyLocale.getWorkspace().getCanonicalVariantName())
                        .getOrCreate()
                        .draft().getOrCreate()
                        .metadata().getOrCreate();
                moduleMetadata.dateModified().set(Calendar.getInstance());
            }

            resolver.commit();

            // TODO: trigger an event to generate the html asynchronous
            response.setStatus(responseCode, "");
        } catch (Exception e) {
            throw new RepositoryException("Error uploading an assembly version", e);
        }
    }
}
