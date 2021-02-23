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
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Component(
        immediate = true,
        service = Filter.class,
        property = {
//                KeycloakOIDCFilter.CONFIG_FILE_PARAM + "=" + "keycloak.json",
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
        methods = {"GET"})
public class KeycloakFilter extends KeycloakOIDCFilter implements Filter {

    private static final Logger log = Logger.getLogger(KeycloakFilter.class.getName());
    private static final String KARAF_ETC = "karaf.etc";
    private  static final String KEYCLOAKOIDCFILTER_CONFIG_FILE_NAME = "keycloak.json";
    protected KeycloakDeployment keycloakDeployment;
//    private KeycloakConfigResolver keycloakConfigResolver;
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
//        String path = file.getPath();
//        String pathParam = filterConfig.getInitParameter(CONFIG_PATH_PARAM);
//        if (pathParam != null){
//            path = pathParam;
//        }
//        InputStream is = filterConfig.getServletContext().getResourceAsStream(path);
        // load config from the file system
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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

        log.info("[" + KeycloakFilter.class.getSimpleName() + "] createKeycloakDeployFrom InputSteam");
        File file = new File( System.getProperty( KARAF_ETC ) + File.separator + KEYCLOAKOIDCFILTER_CONFIG_FILE_NAME );
        // load config from the file system
        InputStream is = new FileInputStream(file);
        keycloakDeployment = createKeycloakDeploymentFrom(is);
        deploymentContext = new AdapterDeploymentContext(keycloakDeployment);

        nodesRegistrationManagement = new NodesRegistrationManagement();
        log.info("[" + KeycloakFilter.class.getSimpleName() + "] Keycloak OIDC Filter");

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
        if (System.getenv("AUTH_SERVER_URL") == null) {
            return true;
        }
        if (skipPattern == null) {
            return false;
        }
        log.info("[" + KeycloakFilter.class.getSimpleName() + "] skipPattern provided.");
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        log.info("[" + KeycloakFilter.class.getSimpleName() + "] Attempt to match skipPattern from requestPath: " + skipPattern.matcher(requestPath).matches());
        return skipPattern.matcher(requestPath).matches();
    }
}
