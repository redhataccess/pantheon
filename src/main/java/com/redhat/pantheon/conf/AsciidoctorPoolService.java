package com.redhat.pantheon.conf;

import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import org.apache.sling.api.resource.Resource;
import org.asciidoctor.Asciidoctor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ben on 4/2/19.
 */
@Component(
        service = AsciidoctorPoolService.class,
        scope = ServiceScope.SINGLETON
)
public class AsciidoctorPoolService {

    // Purposefully specifying the ConcurrentLinkedQueue here because it is thread-safe
    private ConcurrentLinkedQueue<Asciidoctor> queue;
    private LocalFileManagementService localFileManagementService;
    private final Logger log = LoggerFactory.getLogger(AsciidoctorPoolService.class);

    @Activate
    public AsciidoctorPoolService(@Reference LocalFileManagementService localFileManagementService) {
        queue = new ConcurrentLinkedQueue<>();
        this.localFileManagementService = localFileManagementService;
    }

    public synchronized void releaseInstance(Asciidoctor asciidoctor) {
        asciidoctor.unregisterAllExtensions();
        queue.offer(asciidoctor);
        log.trace("Returned Asciidoctor instance to pool, new pool size: {}", queue.size());
    }

    public synchronized Asciidoctor requestInstance(Resource resource) throws IOException {
        Asciidoctor ret = queue.poll();
        if (ret == null) {
            log.trace("No Asciidoctor instances available from pool, creating...");
            ret = Asciidoctor.Factory.create(localFileManagementService.getGemPaths());
        } else {
            log.trace("Reusing Asciidoctor instance from pool");
        }

        ret.javaExtensionRegistry().includeProcessor(
                new SlingResourceIncludeProcessor(resource));
        return ret;
    }
}
