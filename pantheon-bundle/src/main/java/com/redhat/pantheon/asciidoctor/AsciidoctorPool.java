package com.redhat.pantheon.asciidoctor;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.util.pool.ObjectPool;
import com.redhat.pantheon.util.pool.PooledObjectLifecycle;
import org.asciidoctor.Asciidoctor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

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
