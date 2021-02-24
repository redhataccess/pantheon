package com.redhat.pantheon.auth.user;

import com.google.common.collect.Lists;
import com.redhat.pantheon.servlet.ServletUtils;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Custom api to render Auth User info.
 * It provides:
 *   userId: default value "anonymous"
 *   groups: default value "[]"
 *   userType: default value ""
 *
 * @author Lisa Davidson
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet to fetch the auth user info",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/api/userinfo.json")
public class AuthUserInfoServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(AuthUserInfoServlet.class);

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {

        Map userInfo = newHashMap();
        userInfo.put("userID", request.getUserPrincipal().getName());
        userInfo.put("authType", request.getAuthType() != null ? request.getAuthType() : "");
        userInfo.put("groups", Lists.newArrayList());

        User currentUser = request.getResourceResolver().adaptTo(User.class);
        try {
            Iterator<Group> currentUserGroups = currentUser.memberOf();

            if (currentUserGroups != null) {
                while (currentUserGroups.hasNext()) {
                    Group grp = (Group) currentUserGroups.next();
                    String groupName = grp.getID();
                    ((List) userInfo.get("groups")).add(groupName);
                }
            }

        } catch (Exception e) {
            log.debug("[" + AuthUserInfoServlet.class.getSimpleName() + "] currentUser:  " + request.getUserPrincipal().getName() );
        }
        ServletUtils.writeAsJson(response, userInfo);
    }
}
