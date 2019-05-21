package com.redhat.pantheon.sling;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.api.SlingRepositoryInitializer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Created by ben on 3/7/19.
 */
@Component(service = SlingRepositoryInitializer.class)
public class PantheonRepositoryInitializer implements SlingRepositoryInitializer {

    private static final Logger log = LoggerFactory.getLogger(PantheonRepositoryInitializer.class);

    @Override
    public void processRepository(SlingRepository slingRepository) throws Exception {
        initializeRepositoryACLs(slingRepository);
    }

    private void initializeRepositoryACLs(SlingRepository slingRepository) throws RepositoryException {
        JackrabbitSession s = (JackrabbitSession) slingRepository.loginAdministrative(null);
        try {
            // Create and give the pantheon service user permissions to the whole /content path
            try {
                s.getUserManager().createSystemUser("pantheon", null);
                s.save();
                log.info("Created pantheon service account");
            } catch (AuthorizableExistsException aeex) {
                log.info("Pantheon service account already exists");
            }
            assignPermissionToPrincipal(s, "pantheon", "/content", "*", Privilege.JCR_ALL);
            assignPermissionToPrincipal(s,"pantheon-users","/content/repositories", null, Privilege.JCR_WRITE, Privilege.JCR_NODE_TYPE_MANAGEMENT);
            assignPermissionToPrincipal(s,"pantheon-users","/content/modules", null, Privilege.JCR_WRITE, Privilege.JCR_NODE_TYPE_MANAGEMENT);

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
