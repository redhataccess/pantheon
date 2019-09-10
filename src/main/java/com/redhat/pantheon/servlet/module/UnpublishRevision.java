package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.extension.Events;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleRevision;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.post.AbstractPostOperation;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.PostOperation;
import org.apache.sling.servlets.post.PostResponse;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.google.common.collect.Streams.stream;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsLocale;
import static com.redhat.pantheon.util.function.FunctionalUtils.toLastElement;

/**
 * API action which unpublishes the latest released revision for a module, if there is one.
 * This means the "released" pointer is set to null, and the revision should no longer be
 * accessible through the rendering API.
 *
 * @author Carlos Munoz
 */
@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Unpublishes the latest released revision of a module",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:unpublish"
        })
public class UnpublishRevision extends AbstractPostOperation {

    private Events events;

    @Activate
    public UnpublishRevision(@Reference Events events) {
        this.events = events;
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {
        Locale locale = paramValueAsLocale(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE);

        Module module = request.getResource().adaptTo(Module.class);

        // Get the released revision, there should be one
        Optional<ModuleRevision> revisionToUnpublish = module.getReleasedRevision(locale);
        if( !revisionToUnpublish.isPresent() ) {
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED,
                    "The module is not released (published)");
        } else {
            // Released revision is emptied out
            Module.ModuleLocale moduleLocale = module.getModuleLocale(locale);
            String unpublishedRevId = moduleLocale.released.get();
            moduleLocale.released.set( null );

            // if there is no draft version, set the recently unpublished one as draft
            // it is guaranteed to be the latest one
            if (!module.getDraftRevision(locale).isPresent()) {
                moduleLocale.draft.set(unpublishedRevId);
            }

            changes.add(Modification.onModified(module.getPath()));

            // TODO call an extension point similar to ReleaseDraftRevision
            // events.fireModuleRevisionUnpublishedEvent(...);
        }
    }
}
