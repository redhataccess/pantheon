package com.redhat.pantheon.asciidoctor;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.util.pool.PooledObjectLifecycle;
import org.asciidoctor.Asciidoctor;

/**
 * Implementation of the pool lifecycle for {@link Asciidoctor} objects. These objects
 * can be expensive to create so it's better to hold them in a pool.
 *
 * @author Carlos Munoz
 */
class AsciidoctorLifecycle implements PooledObjectLifecycle<Asciidoctor> {

    final GlobalConfig globalConfig;

    AsciidoctorLifecycle(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    @Override
    public Asciidoctor createInstance() {
        return Asciidoctor.Factory.create(globalConfig.getGemPaths());
    }

    @Override
    public void destroy(Asciidoctor obj) {
        obj.shutdown();
    }
}
