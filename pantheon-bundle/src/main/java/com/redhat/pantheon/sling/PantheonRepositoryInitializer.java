package com.redhat.pantheon.sling;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.api.SlingRepositoryInitializer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by ben on 3/7/19.
 */
@Component(service = SlingRepositoryInitializer.class)
public class PantheonRepositoryInitializer implements SlingRepositoryInitializer {

    private static final Logger log = LoggerFactory.getLogger(PantheonRepositoryInitializer.class);

    private ServiceResourceResolverProvider serviceResourceResolverProvider;

    @Activate
    public PantheonRepositoryInitializer(@Reference ServiceResourceResolverProvider serviceResourceResolverProvider) {
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
    }

    @Override
    public void processRepository(SlingRepository slingRepository) throws Exception {
        setSyncServiceUrl(serviceResourceResolverProvider.getServiceResourceResolver());
    }

    private void setSyncServiceUrl(ResourceResolver resourceResolver) throws RepositoryException, PersistenceException {
        if (System.getenv("SYNC_SERVICE_URL") != null) {
            resourceResolver.getResource("/conf/pantheon")
                    .adaptTo(Node.class)
                    .setProperty("pant:syncServiceUrl", System.getenv("SYNC_SERVICE_URL"));
            resourceResolver.commit();
            log.info("Synchronization service URL: " + System.getenv("SYNC_SERVICE_URL"));
        } else {
            log.info("Environment Variable SYNC_SERVICE_URL is not set.");
        }
    }
}
