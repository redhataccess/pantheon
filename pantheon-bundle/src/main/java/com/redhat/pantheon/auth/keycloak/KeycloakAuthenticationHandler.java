package com.redhat.pantheon.auth.keycloak;

import org.apache.sling.auth.core.spi.AuthenticationHandler;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.keycloak.KeycloakSecurityContext;
import org.osgi.service.component.annotations.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * used for authenticating a request that matches the property value
 * defined in AuthenticationHandler.path
 *
 */
@Component(
        name = "com.redhat.pantheon.auth.keycloak.KeycloakAuthenticationHandler",
        property = {
                AuthenticationHandler.PATH_PROPERTY + "=" + "/pantheon/*",
                AuthenticationHandler.TYPE_PROPERTY + "=" + "KEYCLOAK"
        },
        service = AuthenticationHandler.class,
        immediate = true)

public class KeycloakAuthenticationHandler implements org.apache.sling.auth.core.spi.AuthenticationHandler {

    private final Logger log = Logger.getLogger(KeycloakAuthenticationHandler.class.getName());
    private static final String AUTH_TYPE = "KEYCLOAK";

    @Override
    public AuthenticationInfo extractCredentials(
            HttpServletRequest request, HttpServletResponse response) {

        log.info("KeycloakAuthenticationHandler::extractCredentials");
        String extractedUserId = "admin"; //request.getParameter("j_username");
        String extractedPassword = "admin"; // request.getParameter("j_password");
        // KeycloakSecurityContext contains tokenString, AccessToken, idTokenString and IDToken
        KeycloakSecurityContext ctx =
                (KeycloakSecurityContext)
                        request.getSession().getAttribute("org.keycloak.KeycloakSecurityContext");
        KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        log.log(Level.INFO, "KeycloakSecurityContext = {0}", ctx);

        if (keycloakSecurityContext == null) {
            log.info("[" + KeycloakAuthenticationHandler.class.getSimpleName() + "] keycloakSecurityContext is null. ");
        } else {
            log.info("[" + KeycloakAuthenticationHandler.class.getSimpleName() + "] getIdToken: " + keycloakSecurityContext.getIdToken());
        }
        if (ctx != null) {
            log.log(Level.INFO, "username = {0}", ctx.getToken().getPreferredUsername());
            extractedUserId = ctx.getToken().getPreferredUsername();
            return new AuthenticationInfo(AUTH_TYPE, extractedUserId);
        }

        //TODO: use a service account or the identity of the real user instead of admin
        // use of real identify requires user provisioning
//        return new AuthenticationInfo(AUTH_TYPE, "admin", "admin".toCharArray());
        return new AuthenticationInfo(HttpServletRequest.BASIC_AUTH, extractedUserId);
    }

    @Override
    public boolean requestCredentials(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        log.info("KeycloakAuthenticationHandler::requestCredentials");
        try {
            response.getWriter().print("Request");
            log.info("[" + KeycloakAuthenticationHandler.class.getSimpleName() + "] Headername: " + response.getHeaderNames());
        } catch (IOException e) {
            log.info("Error occurred when requesting credentials.");
        }
        return true;
    }

    @Override
    public void dropCredentials(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        log.info("KeycloakAuthenticationHandler::dropCredentials");
    }


    // TODO
//    protected AuthenticationInfo oidcLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {}
}
