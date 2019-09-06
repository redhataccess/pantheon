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
        initializeRepositoryACLs(getSession(slingRepository));
        setSyncServiceUrl(getSession(slingRepository));
    }

    private JackrabbitSession getSession(SlingRepository slingRepository) throws RepositoryException {
        return (JackrabbitSession) slingRepository.loginAdministrative(null);
    }

    private void setSyncServiceUrl(JackrabbitSession s) throws RepositoryException {
        if (System.getenv("SYNC_SERVICE_URL") != null) {
            assignPermissionToPrincipal(s,"pantheon","/conf/pantheon", null, Privilege.JCR_READ, Privilege.JCR_WRITE);
            assignPermissionToPrincipal(s,"pantheon-users","/conf/pantheon", null, Privilege.JCR_READ);
            s.getNode("/conf/pantheon").setProperty("pant:syncServiceUrl",System.getenv("SYNC_SERVICE_URL"));
            s.save();
            s.logout();
        } else {
            log.info("Environment Variable SYNC_SERVICE_URL is not set.");
        }
    }

    private void initializeRepositoryACLs(JackrabbitSession s) throws RepositoryException {
        try {
            // Create and give the pantheon service user permissions to the whole /content path
            try {
                s.getUserManager().createSystemUser("pantheon", null);
                s.save();
                log.info("Created pantheon service account");
            } catch (AuthorizableExistsException aeex) {
                log.info("Pantheon service account already exists");
            }

            // JCR_WRITE and JCR_NODE_TYPE_MANAGEMENT are necessary to push content
            // see: https://docs.adobe.com/docs/en/spec/jsr170/javadocs/jcr-2.0/javax/jcr/security/Privilege.html
            assignPermissionToPrincipal(s, "pantheon", "/content", "*", Privilege.JCR_ALL);
            assignPermissionToPrincipal(s,"pantheon-users","/content/repositories", null, Privilege.JCR_WRITE, Privilege.JCR_NODE_TYPE_MANAGEMENT);
            assignPermissionToPrincipal(s,"pantheon-users","/content/modules", null, Privilege.JCR_WRITE, Privilege.JCR_NODE_TYPE_MANAGEMENT);
            // this is another way to do the above
            AccessControlUtils.addAccessControlEntry(s,
                    "/content/sandbox",
                    AccessControlUtils.getPrincipal(s, "pantheon-users"),
                    privilegesFromNames(s, Privilege.JCR_WRITE, Privilege.JCR_NODE_TYPE_MANAGEMENT),
                    true);

            s.save();
        } catch (Exception ex) {
            log.error("Error initizaling pantheon JCR repository", ex);
        } finally {
            s.logout();
        }
    }

    private void assignPermissionToPrincipal(
            JackrabbitSession js,
            String principalName,
            String nodePath,
            String glob,
            String... privileges
    ) throws RepositoryException {
        PrincipalManager pm = js.getPrincipalManager();

        JackrabbitAccessControlManager am = (JackrabbitAccessControlManager) js.getAccessControlManager();
        JackrabbitAccessControlList list = null;

        Principal pr = pm.getPrincipal(principalName);
        // http://jackrabbit.apache.org/oak/docs/security/accesscontrol/editing.html
        // try if there is an acl that has been set before
        for (AccessControlPolicy policy : am.getPolicies(pr)) {
            if (policy instanceof JackrabbitAccessControlList) {
                list = (JackrabbitAccessControlList) policy;
                break;
            }
        }
        if (list == null) {
            // try if there is an applicable policy
            JackrabbitAccessControlPolicy[] policies = am.getApplicablePolicies(pr);
            for (JackrabbitAccessControlPolicy pol : policies) {
                if (pol instanceof JackrabbitAccessControlList) {
                    list = (JackrabbitAccessControlList) pol;
                    break;
                }
            }
        }

        //Allow principal to modify cachedContent nodes
        List<Privilege> privList = new ArrayList<>(privileges.length);
        for (String s : privileges) {
            privList.add(am.privilegeFromName(s));
        }
        Map<String, Value> restrictions = new HashMap<>();
        ValueFactory vf = js.getValueFactory();
        restrictions.put("rep:nodePath", vf.createValue(nodePath, PropertyType.PATH));
        if (glob != null) {
            restrictions.put("rep:glob", vf.createValue(glob));
        }
        list.addEntry(pr, privList.toArray(new Privilege[privileges.length]), true, restrictions);

        am.setPolicy(list.getPath(), list);
    }
}
