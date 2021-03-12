package com.redhat.pantheon.auth.sso;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletFilterScope;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;

/**
 * A login servlet that handles Keycloak or basic auth request
 *
 * @author Lisa Davidson
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet that handles keycloak or basic auth request",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServlet(
        methods = {"GET", "POST"},
        paths = LoginServlet.PATH_PATTERN
        )
public class LoginServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class.getName());
    private static final String BASIC_AUTH_LOGIN_URI = "/pantheon/#/login";
    static final String PATH_PATTERN = "/auth/login";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String uri = "";
        if (System.getenv("AUTH_SERVER_URL") != null) {
            try {
                uri = System.getenv("SSO_LOGIN_URL") != null ? System.getenv("SSO_LOGIN_URL") : "";

                if (uri.length() > 0) {
                    response.setStatus(302);
                    response.setHeader("Location", uri);
                } else {
                    log.error("Error occurred while creating the Authentication request.");
                }

            } catch (Exception e) {
                log.error("Error occurred while creating the Authentication request.");
            }
        } else {
            log.debug("SSO is not enabled.");
            response.setStatus(302);
            response.setHeader("Location", BASIC_AUTH_LOGIN_URI);
        }
    }
}
