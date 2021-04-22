package com.redhat.pantheon.auth.keycloak;

import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletFilterScope;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.NodesRegistrationManagement;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom KeycloakFilter
 * It defines
 *   keycloak config file
 *   keycloak config skipPattern
 *   keycloak filter pattern
 *
 * @author Lisa Davidson
 */
@Component(
        immediate = true,
        service = Filter.class,
        property = {
                KeycloakOIDCFilter.CONFIG_FILE_PARAM + "=" + "keycloak.json",
                "keycloak.config.skipPattern=(/pantheon/internal/modules.json|/pantheon/builddate.json|/pantheon/fonts/*|/content/repositories.harray.1.json|/starter.html|/bin/browser.html|/content/starter/css/bundle.css|/content/starter/img/sling-logo.svg|/content/starter/img/asf-logo.svg|/content/starter/img/sling-logo.svg|/content/starter/img/gradient.jpg|/content/starter/fonts/OpenSans-Light-webfont.woff|/content/starter/fonts/OpenSans-Regular-webfont.woff|/system/sling.js|/system/*|/pantheon/*.js)",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=" + "/pantheon/*",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=" + "/content/pantheon",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=" + "/content/products",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT
                        + "="
                        + "(osgi.http.whiteboard.context.name=pantheon)",
        })

@SlingServletFilter(scope = {SlingServletFilterScope.REQUEST},
        pattern = "/content/.*",
        methods = {"GET", "POST"})

public class KeycloakFilter extends KeycloakOIDCFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(KeycloakFilter.class.getName());
    private static final String KARAF_ETC = "karaf.etc";
    private static final String KEYCLOAKOIDCFILTER_CONFIG_FILE_NAME = "keycloak.json";
    protected KeycloakDeployment keycloakDeployment;
    private PathBasedKeycloakConfigResolver keycloakConfigResolver;
    /**
     * Constructor that can be used to define a {@code KeycloakConfigResolver} that will be used at
     * initialization to provide the {@code KeycloakDeployment}.
     *
     * @param pathBasedKeycloakConfigResolver the resolver
    */
    public KeycloakFilter(PathBasedKeycloakConfigResolver pathBasedKeycloakConfigResolver) {
        this.keycloakConfigResolver = pathBasedKeycloakConfigResolver;
    }

    public KeycloakFilter() {
        this(null);
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // Read the initialization parameters, whitelist (resources that can be accessed without authentication).
        String skipPatternDefinition = filterConfig.getInitParameter(SKIP_PATTERN_PARAM);
        if (skipPatternDefinition != null) {
            skipPattern = Pattern.compile(skipPatternDefinition, Pattern.DOTALL);
        }
        // Load client configuration information
        File file = new File( System.getProperty( KARAF_ETC ) + File.separator + KEYCLOAKOIDCFILTER_CONFIG_FILE_NAME );

        // load config from the file system
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            log.error("[" + KeycloakFilter.class.getSimpleName() + "] Failed to log config from the file system: " + file.getPath());
        }
        if (is != null) {
        keycloakDeployment = createKeycloakDeploymentFrom(is);
        deploymentContext = new AdapterDeploymentContext(keycloakDeployment);
        filterConfig
                .getServletContext()
                .setAttribute(AdapterDeploymentContext.class.getName(), deploymentContext);
        nodesRegistrationManagement = new NodesRegistrationManagement();
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        //Whitelisted URL, direct release
        if (shouldSkip(request)) {
            chain.doFilter(req, res);
            return;
        }
        super.doFilter(req, res, chain);
    }

    private KeycloakDeployment createKeycloakDeploymentFrom(InputStream is) {
        if (is == null) {
            log.info("[" +KeycloakFilter.class.getSimpleName() + "] No adapter configuration. Keycloak is unconfigured and will deny all requests.");
            return new KeycloakDeployment();
        }
        return KeycloakDeploymentBuilder.build(is);
    }

    private boolean shouldSkip(HttpServletRequest request) {
        // Check request header to allow basic auth.
        // Check if AUTH_SERVER_URL is configured, fall back to basic auth otherwise.
        if ((request.getHeader("Authorization") != null && request.getHeader("Authorization").startsWith("Basic "))
                || System.getenv("AUTH_SERVER_URL") == null) {
            return true;
        }
        if (skipPattern == null) {
            return false;
        }

        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        return skipPattern.matcher(requestPath).matches();
    }
}