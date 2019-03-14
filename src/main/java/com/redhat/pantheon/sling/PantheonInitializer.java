package com.redhat.pantheon.sling;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.api.SlingRepositoryInitializer;
import org.osgi.service.component.annotations.Component;

import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ben on 3/7/19.
 */
@Component(service = SlingRepositoryInitializer.class)
public class PantheonInitializer implements SlingRepositoryInitializer {

    @Override
    public void processRepository(SlingRepository slingRepository) throws Exception {
        //May be a good place to autocreate users at some point in the future
        Session s = slingRepository.loginAdministrative(null);
        try {
            JackrabbitSession js = (JackrabbitSession) s;
            PrincipalManager pm = js.getPrincipalManager();
            Principal anon = pm.getPrincipal("anonymous");

            JackrabbitAccessControlManager am = (JackrabbitAccessControlManager) js.getAccessControlManager();
            JackrabbitAccessControlList list = null;

            // http://jackrabbit.apache.org/oak/docs/security/accesscontrol/editing.html
            // try if there is an acl that has been set before
            for (AccessControlPolicy policy : am.getPolicies(anon)) {
                if (policy instanceof JackrabbitAccessControlList) {
                    list = (JackrabbitAccessControlList) policy;
                    break;
                }
            }
            if (list == null) {
                // try if there is an applicable policy
                JackrabbitAccessControlPolicy[] policies = am.getApplicablePolicies(anon);
                for (JackrabbitAccessControlPolicy pol : policies) {
                    if (pol instanceof JackrabbitAccessControlList) {
                        list = (JackrabbitAccessControlList) pol;
                        break;
                    }
                }
            }

            //Allow anonymous to modify cachedContent nodes
            Privilege[] privileges = new Privilege[] { am.privilegeFromName(Privilege.JCR_MODIFY_PROPERTIES) };
            Map<String, Value> restrictions = new HashMap<>();
            ValueFactory vf = js.getValueFactory();
            restrictions.put("rep:nodePath", vf.createValue("/content/modules", PropertyType.PATH));
            // http://jackrabbit.apache.org/api/2.16/org/apache/jackrabbit/core/security/authorization/GlobPattern.html
            restrictions.put("rep:glob", vf.createValue("/*/cachedContent*")); // No idea why the trailing * is necessary but it doesn't work without it
            list.addEntry(anon, privileges, true, restrictions);

            am.setPolicy(list.getPath(), list);

            s.save();
        } catch (Exception e) {
            e.printStackTrace(); //FIXME
        } finally {
            s.logout();
        }
    }
}
