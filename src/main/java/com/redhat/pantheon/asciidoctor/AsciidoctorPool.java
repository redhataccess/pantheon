package com.redhat.pantheon.asciidoctor;

import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import com.redhat.pantheon.conf.LocalFileManagementService;
import com.redhat.pantheon.util.pool.ObjectPool;
import com.redhat.pantheon.util.pool.PooledObjectLifecycle;
import org.apache.sling.api.resource.Resource;
import org.asciidoctor.Asciidoctor;
import org.osgi.service.component.annotations.*;

/**
 * A pool of {@link Asciidoctor} instances. This component has a set initial and maxium size.
 *
 * @author Carlos Munoz
 */
@Component(
        service = AsciidoctorPool.class,
        scope = ServiceScope.SINGLETON
)
public class AsciidoctorPool extends ObjectPool<Asciidoctor> {

    // Hardcoding these values for now
    protected static final int INITIAL_SIZE = 5;
    protected static final int MAX_SIZE = 10;

    @Activate
    public AsciidoctorPool(@Reference LocalFileManagementService localFileManagementService) {
        this(new AsciidoctorLifecycle(localFileManagementService));
    }

    /**
     * Protected constructor for unit testing
     */
    AsciidoctorPool(PooledObjectLifecycle<Asciidoctor> objectLifecycle) {
        super(objectLifecycle, INITIAL_SIZE, MAX_SIZE);
    }

    /**
     * Specific implementation of the {@link #borrowObject()} method which takes a
     * {@link Resource} as a parameter. This method should be called when using asciidoctor to generate
     * output from a sling Resource object.
     * @param base The base resource to use
     * @return An {@link Asciidoctor} instance to generate output based on a sling {@link Resource}
     */
    public Asciidoctor borrowAsciidoctorObject(Resource base) {
        Asciidoctor asciidoctor = super.borrowObject();
        asciidoctor.javaExtensionRegistry().includeProcessor(
                new SlingResourceIncludeProcessor(base));
        return asciidoctor;
    }

    @Override
    @Deactivate
    public synchronized void close() throws Exception {
        super.close();
    }
}
