package com.redhat.pantheon.auth.sso;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
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
                "sling.servlet.methods={GET, POST}",
                "sling.servlet.paths=/auth/login"
                },
        immediate = true)
public class LoginServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class.getName());
    private static final String BASIC_AUTH_LOGIN_URI = "/pantheon/#/login";

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
