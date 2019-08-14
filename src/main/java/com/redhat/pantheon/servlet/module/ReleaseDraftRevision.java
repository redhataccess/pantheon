package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.Module.ModuleLocale;
import com.redhat.pantheon.model.module.ModuleRevision;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.servlets.post.AbstractPostOperation;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.PostOperation;
import org.apache.sling.servlets.post.PostResponse;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;
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
    @Override
    protected void doRun(SlingHttpServletRequest request, PostResponse response, List<Modification> changes) throws RepositoryException {
        Locale locale = paramValueAsLocale(request, "locale", GlobalConfig.DEFAULT_MODULE_LOCALE);

        Module module = request.getResource().adaptTo(Module.class);

        // Get the draft revision, there should be one
        Optional<ModuleRevision> draftRevision = module.getDraftRevision(locale);
        if( !draftRevision.isPresent() ) {
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED,
                    "The module doesn't have a draft revision to be released");
        } else {
            // Draft becomes the new released version
            ModuleLocale moduleLocale = module.getModuleLocale(locale);
            moduleLocale.released.set( moduleLocale.draft.get() );
            moduleLocale.draft.set( null );
            changes.add(Modification.onModified(module.getPath()));
        }
    }
}
