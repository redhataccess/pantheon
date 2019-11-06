package com.redhat.pantheon.sling;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.api.SlingRepositoryInitializer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        setSyncServiceUrl();
    }

    private void setSyncServiceUrl() throws RepositoryException, PersistenceException {
        try (ResourceResolver resourceResolver = serviceResourceResolverProvider.getServiceResourceResolver()) {
            String syncServiceUrl = getSyncServiceUrl();
            if (syncServiceUrl != null) {
                resourceResolver.getResource("/conf/pantheon")
                        .adaptTo(ModifiableValueMap.class)
                        .put("pant:syncServiceUrl", syncServiceUrl);
                resourceResolver.commit();
                log.info("Synchronization service URL: " + syncServiceUrl);
            } else {
                log.info("Environment Variable SYNC_SERVICE_URL is not set.");
            }
        }
    }

    /**
     * Retrieves the environment variable value for the sync service url
     */
    String getSyncServiceUrl() {
        return System.getenv("SYNC_SERVICE_URL");
    }
}
