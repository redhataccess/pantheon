package com.redhat.pantheon.asciidoctor;

import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import com.redhat.pantheon.conf.GlobalConfig;
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
    protected static final int INITIAL_SIZE = 1;
    protected static final int MAX_SIZE = 10;

    @Activate
    public AsciidoctorPool(@Reference GlobalConfig globalConfig) {
        this(new AsciidoctorLifecycle(globalConfig));
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
    public Asciidoctor borrowObject(Resource base) {
        Asciidoctor asciidoctor = super.borrowObject();
        try {
            // TODO - Leave it up to the caller of this method to set up their own extensions.
            // TODO - For now, this works because we're interested in processing Resource objects 100% of the time.
            // TODO - However, that may not always be so. When that time comes, this method should not set up any
            // TODO - extensions, and then this try/catch->returnObject construct can disappear as well.
            asciidoctor.javaExtensionRegistry().includeProcessor(
                    new SlingResourceIncludeProcessor(base));
            return asciidoctor;
        } catch (Exception e) {
            returnObject(asciidoctor);
            throw e;
        }
    }

    @Override
    public void returnObject(Asciidoctor obj) {
        obj.unregisterAllExtensions();
        super.returnObject(obj);
    }

    @Override
    @Deactivate
    public synchronized void close() throws Exception {
        super.close();
    }
}
