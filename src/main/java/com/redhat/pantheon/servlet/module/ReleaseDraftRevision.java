package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.extension.Events;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.Module.ModuleLocale;
import com.redhat.pantheon.model.module.ModuleRevision;
import org.apache.sling.api.SlingHttpServletRequest;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsLocale;

@Component(
        service = PostOperation.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Releases the latest draft revision of a module",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                PostOperation.PROP_OPERATION_NAME + "=pant:release"
        })
public class ReleaseDraftRevision extends AbstractPostOperation {

    private Events events;

    @Activate
    public ReleaseDraftRevision(@Reference Events events) {
        this.events = events;
    }

    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {
        Locale locale = paramValueAsLocale(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE);

        Module module = request.getResource().adaptTo(Module.class);

        // Get the draft revision, there should be one
        Optional<ModuleRevision> revisionToRelease = module.getDraftRevision(locale);
        if( !revisionToRelease.isPresent() ) {
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED,
                    "The module doesn't have a draft revision to be released");
        } else {
            // Draft becomes the new released version
            ModuleLocale moduleLocale = module.getModuleLocale(locale);
            moduleLocale.released.set( moduleLocale.draft.get() );
            moduleLocale.draft.set( null );
            // set the published date on the released revision
            revisionToRelease.get()
                    .metadata.getOrCreate()
                    .datePublished.set(Calendar.getInstance());
            changes.add(Modification.onModified(module.getPath()));

            // call the extension point
            events.fireModuleRevisionPublishedEvent(moduleLocale.released.getReference());
        }
    }
}
