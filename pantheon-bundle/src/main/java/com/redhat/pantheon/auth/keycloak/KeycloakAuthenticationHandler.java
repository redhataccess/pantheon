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

@Component(
        name = "com.redhat.pantheon.auth.keycloak.KeycloakAuthenticationHandler",
        property = {
                AuthenticationHandler.PATH_PROPERTY + "=" + "/",
                AuthenticationHandler.TYPE_PROPERTY + "=" + "KEYCLOAK"
        },
        service = AuthenticationHandler.class,
        immediate = true)

public class KeycloakAuthenticationHandler implements org.apache.sling.auth.core.spi.AuthenticationHandler {

    private static final Logger LOG = Logger.getLogger(KeycloakAuthenticationHandler.class.getName());
    private static final String AUTH_TYPE = "KEYCLOAK";

    @Override
    public AuthenticationInfo extractCredentials(
            HttpServletRequest request, HttpServletResponse response) {

        LOG.fine("KeycloakAuthenticationHandler::extractCredentials");

        // KeycloakSecurityContext contains tokenString, AccessToken, idTokenString and IDToken
        KeycloakSecurityContext ctx =
                (KeycloakSecurityContext)
                        request.getSession().getAttribute("org.keycloak.KeycloakSecurityContext");
        LOG.log(Level.FINE, "KeycloakSecurityContext = {0}", ctx);

        if (ctx != null) {
            LOG.log(Level.FINE, "username = {0}", ctx.getToken().getPreferredUsername());
        }

        //TODO: use a service account or the identity of the real user instead of admin
        // use of real identify requires user provisioning
        return new AuthenticationInfo(AUTH_TYPE, "admin", "admin".toCharArray());
    }

    @Override
    public boolean requestCredentials(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        LOG.fine("KeycloakAuthenticationHandler::requestCredentials");
        return true;
    }

    @Override
    public void dropCredentials(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        LOG.fine("KeycloakAuthenticationHandler::dropCredentials");
    }


    // TODO
//    protected AuthenticationInfo oidcLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {}
}
