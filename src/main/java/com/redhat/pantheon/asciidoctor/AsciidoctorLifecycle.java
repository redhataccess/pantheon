package com.redhat.pantheon.asciidoctor;

import com.redhat.pantheon.conf.LocalFileManagementService;
import com.redhat.pantheon.util.pool.PooledObjectLifecycle;
import org.asciidoctor.Asciidoctor;

/**
 * Implementation of the pool lifecycle for {@link Asciidoctor} objects. These objects
 * can be expensive to create so it's better to hold them in a pool.
 *
 * @author Carlos Munoz
 */
class AsciidoctorLifecycle implements PooledObjectLifecycle<Asciidoctor> {

    final LocalFileManagementService localFileManagementService;

    AsciidoctorLifecycle(LocalFileManagementService localFileManagementService) {
        this.localFileManagementService = localFileManagementService;
    }

    @Override
    public Asciidoctor createInstance() {
        return Asciidoctor.Factory.create(localFileManagementService.getGemPaths());
    }

    @Override
    public void destroy(Asciidoctor obj) {
        obj.shutdown();
    }
}
