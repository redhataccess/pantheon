package com.redhat.pantheon.sling;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.api.SlingRepositoryInitializer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils.privilegesFromNames;

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
        setSyncServiceUrl(getSession(slingRepository));
    }

    private JackrabbitSession getSession(SlingRepository slingRepository) throws RepositoryException {
        return (JackrabbitSession) slingRepository.loginAdministrative(null);
    }

    private void setSyncServiceUrl(JackrabbitSession s) throws RepositoryException {
        if (System.getenv("SYNC_SERVICE_URL") != null) {
            log.info("Synchronization service URL: " + System.getenv("SYNC_SERVICE_URL"));
        } else {
            log.info("Environment Variable SYNC_SERVICE_URL is not set.");
        }
    }
}
