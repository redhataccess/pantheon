package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.extension.Events;
import com.redhat.pantheon.extension.events.ModuleVersionPublishedEvent;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.module.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.PostOperation;
import org.apache.sling.servlets.post.PostResponse;
import org.apache.sling.servlets.post.SlingPostProcessor;
import org.apache.sling.servlets.post.impl.operations.AbstractPostOperation;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.redhat.pantheon.jcr.JcrResources.rename;
import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsLocale;

@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Releases the latest draft version of a module",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:publish"
        })
public class PublishDraftVersion extends AbstractPostOperation {

    private Events events;
    private AsciidoctorService asciidoctorService;

    @Activate
    public PublishDraftVersion(@Reference Events events,
                               @Reference AsciidoctorService asciidoctorService) {
        this.events = events;
        this.asciidoctorService = asciidoctorService;
    }

    private Module getModule(SlingHttpServletRequest request) {
        return request.getResource().adaptTo(Module.class);
    }

    private Locale getLocale(SlingHttpServletRequest request) {
        return paramValueAsLocale(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE);
    }

    private String getVariant(SlingHttpServletRequest request) {
        return paramValue(request, "variant", ModuleVariant.DEFAULT_VARIANT_NAME);
    }

    @Override
    public void run(SlingHttpServletRequest request, PostResponse response, SlingPostProcessor[] processors) {
        super.run(request, response, processors);
        if (response.getError() == null) {
            // call the extension point
            Locale locale = getLocale(request);
            Module module = getModule(request);
            String variant = getVariant(request);
            ModuleVersion moduleVersion = module.moduleLocale(locale).get()
                    .variants().get()
                    .variant(variant).get()
                    .released().get();

            // Regenerate the module once more
            asciidoctorService.getModuleHtml(module, locale, variant, false, new HashMap(), true);
            events.fireEvent(new ModuleVersionPublishedEvent(moduleVersion), 15);
        }
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) {
        Locale locale = getLocale(request);
        Module module = getModule(request);
        String variant = getVariant(request);

        // Get the draft version, there should be one
        Optional<ModuleVersion> versionToRelease = module.getDraftVersion(locale, variant);
        if( !versionToRelease.isPresent() ) {
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED,
                    "The module doesn't have a draft version to be released");
        } else if (versionToRelease.get().metadata().getOrCreate().productVersion().get() == null
        		||  versionToRelease.get().metadata().getOrCreate().productVersion().get().isEmpty()) {
        	// Check if productVersion is set
        	response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED,
                    "The version to be released doesn't have productVersion metadata");
        } else if (versionToRelease.get().metadata().getOrCreate().urlFragment().get() == null
        		||  versionToRelease.get().metadata().getOrCreate().urlFragment().get().isEmpty()) {
        	// Check if urlFragment is set
        	response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED,
                    "The version to be released doesn't have urlFragment metadata");
        } else {
            // Draft becomes the new released version
            ModuleVariant moduleVariant = traverseFrom(module)
                    .toChild(m -> module.moduleLocale(locale))
                    .toChild(ModuleLocale::variants)
                    .toChild(variants -> variants.variant(variant))
                    .get();
            moduleVariant.releaseDraft();
            changes.add(Modification.onModified(module.getPath()));
            // source/draft becomes source/released
            FileResource draftSource = traverseFrom(module)
                    .toChild(m -> module.moduleLocale(locale))
                    .toChild(ModuleLocale::source)
                    .toChild(sourceContent -> sourceContent.draft())
                    .get();
            // Check for released version
            Optional<HashableFileResource> releasedSource = traverseFrom(module)
                    .toChild(m -> module.moduleLocale(locale))
                    .toChild(ModuleLocale::source)
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
        }
    }
}
