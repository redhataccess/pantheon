package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.extension.Events;
import com.redhat.pantheon.extension.events.assembly.AssemblyVersionPublishedEvent;
import com.redhat.pantheon.extension.events.module.ModuleVersionPublishedEvent;
import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.HashableFileResource;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.assembly.AssemblyVersion;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentLocale;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.document.DocumentVersion;
import com.redhat.pantheon.model.module.*;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.apache.sling.servlets.post.*;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.redhat.pantheon.jcr.JcrResources.rename;
import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsLocale;

@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Releases the latest draft version of a document (Assembly or Module)",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:publish"
        })
public class PublishDraftVersion extends AbstractPostOperation {

    private Events events;
    private AsciidoctorService asciidoctorService;
    private ServiceResourceResolverProvider serviceResourceResolverProvider;
    private Logger logger = LoggerFactory.getLogger(PublishDraftVersion.class);

    @Activate
    public PublishDraftVersion(@Reference Events events,
                               @Reference AsciidoctorService asciidoctorService,
                               @Reference ServiceResourceResolverProvider serviceResourceResolverProvider) {
        this.events = events;
        this.asciidoctorService = asciidoctorService;
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
    }

    private Locale getLocale(SlingHttpServletRequest request) {
        return paramValueAsLocale(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE);
    }

    private String getVariant(SlingHttpServletRequest request) {
        return paramValue(request, "variant", DocumentVariant.DEFAULT_VARIANT_NAME);
    }

    @Override
    public void run(SlingHttpServletRequest request, PostResponse response, SlingPostProcessor[] processors) {
        logger.debug("Operation Publishinging draft version started");
        long startTime = System.currentTimeMillis();
        super.run(request, response, processors);
        if (response.getError() == null) {
            // call the extension point
            Locale locale = getLocale(request);
            Document document = request.getResourceResolver().getResource(request.getResource().getPath()).adaptTo(Document.class);
            String variant = getVariant(request);
            DocumentVersion documentVersion = document.locale(locale).get()
                    .variants().get()
                    .variant(variant).get()
                    .released().get();

            // Regenerate the document once more
            asciidoctorService.getModuleHtml(document, locale, variant, false, new HashMap(), true);

            if(PantheonConstants.RESOURCETYPE_ASSEMBLY.equals(document.getResourceType())){
                events.fireEvent(new AssemblyVersionPublishedEvent(documentVersion.adaptTo(AssemblyVersion.class)), 15);
            }else{
                events.fireEvent(new ModuleVersionPublishedEvent(documentVersion.adaptTo(ModuleVersion.class)), 15);
            }
        }
        log.debug("Operation Publishinging draft version,  completed");
        long elapseTime = System.currentTimeMillis() - startTime;
        log.debug("Total elapsed http request/response time in milliseconds: " + elapseTime);
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws  RepositoryException{
        try {
            boolean canPublish = false;
            Session session = request.getResourceResolver().adaptTo(Session.class);
            UserManager userManager = AccessControlUtil.getUserManager(session);
            Iterator<Group> groupIterator = userManager.getAuthorizable(session.getUserID()).memberOf();
            while (groupIterator.hasNext()) {
                Authorizable group = groupIterator.next();
                if (group.isGroup() && PantheonConstants.PANTHEON_PUBLISHERS.equalsIgnoreCase(group.getID())) {
                    canPublish = true;
                    break;
                }
            }
            ResourceResolver serviceResourceResolver = request.getResourceResolver();
            if(canPublish) {
                serviceResourceResolver = serviceResourceResolverProvider.getServiceResourceResolver();
            }
            Document document = serviceResourceResolver.getResource(request.getResource().getPath()).adaptTo(Document.class);
            Locale locale = getLocale(request);
            String variant = getVariant(request);
            // Get the draft version, there should be one
            Optional<? extends DocumentVersion> versionToRelease = document.getDraftVersion(locale, variant);
            if (!versionToRelease.isPresent()) {
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED,
                        "The document doesn't have a draft version to be released");
                return;
            } else if (versionToRelease.get().metadata().getOrCreate().productVersion().get() == null
                    || versionToRelease.get().metadata().getOrCreate().productVersion().get().isEmpty()) {
                // Check if productVersion is set
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED,
                        "The version to be released doesn't have productVersion metadata");
                return;
            } else {
                // Draft becomes the new released version
                DocumentVariant docVariant = traverseFrom(document)
                        .toChild(d -> d.locale(locale))
                        .toChild(DocumentLocale::variants)
                        .toChild(variants -> variants.variant(variant))
                        .get();
                docVariant.releaseDraft();
                changes.add(Modification.onModified(document.getPath()));
                // source/draft becomes source/released
                FileResource draftSource = traverseFrom(document)
                        .toChild(d -> d.locale(locale))
                        .toChild(DocumentLocale::source)
                        .toChild(sourceContent -> sourceContent.draft())
                        .get();
                // Check for released version
                Optional<HashableFileResource> releasedSource = traverseFrom(document)
                        .toChild(d -> d.locale(locale))
                        .toChild(DocumentLocale::source)
                        .toChild(sourceContent -> sourceContent.released())
                        .getAsOptional();
                if (draftSource != null) {
                    if (releasedSource.isPresent()) {
                        try {
                            releasedSource.get().delete();
                        } catch (PersistenceException e) {
                            throw new RuntimeException("Failed to remove source/released: " + releasedSource.get().getPath());
                        }
                    }
                    try {
                        rename(draftSource, "released");
                    } catch (RepositoryException e) {
                        throw new RuntimeException("Cannot find source/draft: " + draftSource.getPath());
                    }
                }
                if(serviceResourceResolver.hasChanges()) {
                    serviceResourceResolver.commit();
                }
                return;
            }
        }catch (Exception ex){
            throw new RepositoryException(ex.getMessage());
        }
    }
}
