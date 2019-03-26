package com.redhat.pantheon.sling;

import com.redhat.pantheon.conf.LocalFileManagementService;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.api.SlingRepositoryInitializer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
public class PantheonInitializer implements SlingRepositoryInitializer {

    private static final Logger log = LoggerFactory.getLogger(SlingRepositoryInitializer.class);

    private LocalFileManagementService localFileManagementService;

    @Activate
    public PantheonInitializer(
            @Reference LocalFileManagementService localFileManagementService) {
        this.localFileManagementService = localFileManagementService;
    }

    @Override
    public void processRepository(SlingRepository slingRepository) throws Exception {
        initializeRepositoryACLs(slingRepository);
        localFileManagementService.initializeTemplateDirectories();
    }

    private void initializeRepositoryACLs(SlingRepository slingRepository) throws RepositoryException {
        JackrabbitSession s = (JackrabbitSession) slingRepository.loginAdministrative(null);
        try {
            User admin = (User) s.getUserManager().getAuthorizable("admin");
            admin.changePassword("ccsadmin"); // FIXME - hardcoding admin passwords is a Bad Thing

            // http://jackrabbit.apache.org/api/2.16/org/apache/jackrabbit/core/security/authorization/GlobPattern.html
            assignPermissionToPrincipal(s, "anonymous", "/content/modules", "/*/cachedContent*", Privilege.JCR_MODIFY_PROPERTIES); // No idea why the trailing * is necessary but it doesn't work without it
            assignPermissionToPrincipal(s, "demo", "/content/modules", null, Privilege.JCR_WRITE, Privilege.JCR_NODE_TYPE_MANAGEMENT);
            s.save();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
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
