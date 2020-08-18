package com.redhat.pantheon.servlet;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.extension.Events;
import com.redhat.pantheon.extension.events.assembly.AssemblyVersionUnpublishedEvent;
import com.redhat.pantheon.extension.events.module.ModuleVersionUnpublishedEvent;
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
import org.apache.sling.servlets.post.AbstractPostOperation;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.PostOperation;
import org.apache.sling.servlets.post.PostResponse;
import org.apache.sling.servlets.post.SlingPostProcessor;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.redhat.pantheon.jcr.JcrResources.rename;
import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsLocale;

/**
 * API action which unpublishes the latest released version for a document (Assembly/Module), if there is one.
 * This means the "released" pointer is set to null, and the version should no longer be
 * accessible through the rendering API.
 *
 * @author Carlos Munoz
 */
@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Unpublishes the latest released version of a document (Assembly or Module)",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:unpublish"
        })
public class UnpublishVersion extends AbstractPostOperation {

    private Events events;
    private ServiceResourceResolverProvider serviceResourceResolverProvider;
    private Logger logger = LoggerFactory.getLogger(PublishDraftVersion.class);

    @Activate
    public UnpublishVersion(@Reference Events events,
                            @Reference ServiceResourceResolverProvider serviceResourceResolverProvider) {
        this.events = events;
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
    }

    private Document getDocument(SlingHttpServletRequest request) {
        return request.getResource().adaptTo(Document.class);
    }

    private Locale getLocale(SlingHttpServletRequest request) {
        return paramValueAsLocale(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE);
    }

    private String getVariant(SlingHttpServletRequest request) {
        return paramValue(request, "variant", DocumentVariant.DEFAULT_VARIANT_NAME);
    }

    @Override
    public void run(SlingHttpServletRequest request, PostResponse response, SlingPostProcessor[] processors) {
        logger.debug("Operation UnPublishinging draft version started");
        long startTime = System.currentTimeMillis();
        super.run(request, response, processors);
        if (response.getError() == null) {
            // call the extension point
            Locale locale = getLocale(request);
            Document document = getDocument(request);
            String variant = getVariant(request);
            DocumentVersion documentVersion = document.locale(locale).get()
                    .variants().get()
                    .variant(variant).get()
                    .draft().get();

            // TODO We need to change the event so that the right variant is processed
            if(PantheonConstants.RESOURCETYPE_ASSEMBLY.equals(document.getResourceType())){
                events.fireEvent(new AssemblyVersionUnpublishedEvent(documentVersion.adaptTo(AssemblyVersion.class)), 15);
            }else{
                events.fireEvent(new ModuleVersionUnpublishedEvent(documentVersion.adaptTo(ModuleVersion.class)), 15);
            }
        }
        log.debug("Operation UnPublishinging draft version,  completed");
        long elapseTime = System.currentTimeMillis() - startTime;
        log.debug("Total elapsed http request/response time in milliseconds: " + elapseTime);
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException{
        try {
            boolean canUnPublish = false;
            Session session = request.getResourceResolver().adaptTo(Session.class);
            UserManager userManager = AccessControlUtil.getUserManager(session);
            Iterator<Group> groupIterator = userManager.getAuthorizable(session.getUserID()).memberOf();
            while (groupIterator.hasNext()) {
                Authorizable group = groupIterator.next();
                if (group.isGroup() && PantheonConstants.PANTHEON_PUBLISHERS.equalsIgnoreCase(group.getID())) {
                    canUnPublish = true;
                    break;
                }
            }
            ResourceResolver serviceResourceResolver = request.getResourceResolver();
            if(canUnPublish) {
                serviceResourceResolver = serviceResourceResolverProvider.getServiceResourceResolver();
            }
            Document document = getDocument(request);
            Locale locale = getLocale(request);
            String variant = getVariant(request);

            // Get the released version, there should be one
            Optional<? extends DocumentVersion> foundVariant = document.getReleasedVersion(locale, variant);

            if(!foundVariant.isPresent()) {
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED,
                        "The document is not released (published)");
                return;
            } else {
                foundVariant.get()
                        .getParent()
                        .revertReleased();

                changes.add(Modification.onModified(document.getPath()));
                // Change source/released to source/draft
                Optional<HashableFileResource> draftSource = traverseFrom(document)
                        .toChild(d -> d.locale(locale))
                        .toChild(DocumentLocale::source)
                        .toChild(sourceContent -> sourceContent.draft())
                        .getAsOptional();
                FileResource releasedSource = traverseFrom(document)
                        .toChild(d -> d.locale(locale))
                        .toChild(DocumentLocale::source)
                        .toChild(sourceContent -> sourceContent.released())
                        .get();
                if (draftSource.isPresent()) {
                    // Delete released
                    try {
                        releasedSource.delete();
                    } catch (PersistenceException e) {
                        throw new RuntimeException("Failed to delete source/released: " + releasedSource.getPath());
                    }

                } else {
                    try {
                        rename(releasedSource, "draft");
                    } catch (RepositoryException e) {
                        throw new RuntimeException("Cannot rename source/released to source/draft: " + releasedSource.getPath());
                    }
                }
                if(serviceResourceResolver.hasChanges()) {
                    serviceResourceResolver.commit();
                }
            }
            return;
        }catch (Exception ex){
            throw new RepositoryException(ex.getMessage());
        }
    }
}
