package com.redhat.pantheon.auth.login;

import com.redhat.pantheon.auth.keycloak.KeycloakAuthenticationHandler;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.redhat.pantheon.servlet.ServletUtils.writeAsJson;

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
            log.info("SSO is not enabled.");
            uri = "/pantheon/#/login";
            response.setStatus(302);
            response.setHeader("Location", uri);
        }
    }
}
