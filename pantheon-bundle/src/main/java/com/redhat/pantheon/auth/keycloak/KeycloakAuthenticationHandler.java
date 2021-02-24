package com.redhat.pantheon.auth.keycloak;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.auth.core.spi.AuthenticationHandler;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.apache.sling.jcr.api.SlingRepository;
import org.keycloak.KeycloakSecurityContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * used for authenticating a request that matches the property value
 * defined in AuthenticationHandler.path
 *
 * @author Lisa Davidson
 */
@Component(
        name = "com.redhat.pantheon.auth.keycloak.KeycloakAuthenticationHandler",
        property = {
                AuthenticationHandler.PATH_PROPERTY + "=" + "/",
                AuthenticationHandler.TYPE_PROPERTY + "=" + KeycloakAuthenticationHandler.AUTH_TYPE
        },
        service = AuthenticationHandler.class,
        immediate = true)

public class KeycloakAuthenticationHandler implements org.apache.sling.auth.core.spi.AuthenticationHandler {

    private final Logger log = Logger.getLogger(KeycloakAuthenticationHandler.class.getName());
    public static final String AUTH_TYPE = "KEYCLOAK";
    private static final String DEFAULT_GROUP = "pantheon-authors";

    private Session session;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    @Reference
    private SlingRepository repository;

    @Override
    public AuthenticationInfo extractCredentials(
            HttpServletRequest request, HttpServletResponse response) {

        if (System.getenv("AUTH_SERVER_URL") != null) {
            String extractedUserId = "";

            // KeycloakSecurityContext contains tokenString, AccessToken, idTokenString and IDToken
            KeycloakSecurityContext ctx =
                (KeycloakSecurityContext)
                        request.getSession().getAttribute("org.keycloak.KeycloakSecurityContext");
            log.info("[" + KeycloakAuthenticationHandler.class.getSimpleName() + "] KeycloakSecurityContext:" + ctx);
            if (ctx != null) {
                log.info("[" + KeycloakAuthenticationHandler.class.getSimpleName() + "] username: " + ctx.getToken().getPreferredUsername());

                ResourceResolver resolver = null;
                try {
                    extractedUserId = ctx.getToken().getPreferredUsername();

                    if (extractedUserId != null) {
                        resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
                        session = resolver.adaptTo(Session.class);

                        //Create a UserManager instance from the session object
                        UserManager userManager = ((JackrabbitSession) session).getUserManager();

                        JackrabbitSession js = (JackrabbitSession) session;

                        Authorizable user = userManager.getAuthorizable(extractedUserId);
                        if (user == null) {
                            log.info("[" + KeycloakAuthenticationHandler.class.getSimpleName() + "] user does not exist in the system. Attempt to create new user: " + extractedUserId);

                            userManager.createUser(extractedUserId, extractedUserId);
                            // Use "pantheon-authors" as the default group
                            Group group = (Group) userManager.getAuthorizable(DEFAULT_GROUP);
                            if (group != null) {
                                group.addMember(userManager.getAuthorizable(extractedUserId));
                                log.info("[" + KeycloakAuthenticationHandler.class.getSimpleName() + "] add user: " + extractedUserId + " to group: " + DEFAULT_GROUP);
                            }
                            session.save();
                            session.logout();
                            resolver.commit();
                        }
                        Session session = this.repository.login(new SimpleCredentials(extractedUserId, extractedUserId.toCharArray()));
                            if (session != null) {
                            return new AuthenticationInfo(AUTH_TYPE, session.getUserID(), session.getUserID().toCharArray());
                        }
                    }
                } catch (Exception e) {
                    log.warning("[" + KeycloakAuthenticationHandler.class.getSimpleName() + "] Exception in extractCredentials while processing the request" + e.getMessage());
                } finally {
                    if (resolver != null && resolver.isLive()) {
                        resolver.close();
                    }
                }
            }
        } else {
            log.info("[" + KeycloakAuthenticationHandler.class.getSimpleName() + "] AUTH_SERVER_URL not defined. Use basic auth instead...");
        }
        return null;
    }

    @Override
    public boolean requestCredentials(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            response.getWriter().print("Request");
        } catch (IOException e) {
            log.info("[" +KeycloakAuthenticationHandler.class.getSimpleName() + "] Error occurred when requesting credentials.");
        }
        return true;
    }

    @Override
    public void dropCredentials(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
    }

}
