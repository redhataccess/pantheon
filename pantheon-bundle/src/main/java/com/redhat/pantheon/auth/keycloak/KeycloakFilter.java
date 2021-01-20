

//package com.redhat.pantheon.auth.keycloak;
//
//import org.apache.sling.servlets.annotations.SlingServletFilter;
//import org.apache.sling.servlets.annotations.SlingServletFilterScope;
//import org.keycloak.adapters.AdapterDeploymentContext;
//import org.keycloak.adapters.KeycloakDeployment;
//import org.keycloak.adapters.KeycloakDeploymentBuilder;
//import org.keycloak.adapters.NodesRegistrationManagement;
//import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
//
//import javax.servlet.*;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.logging.Logger;
//import java.util.regex.Pattern;
//
//@Component(
//        immediate = true,
//        service = Filter.class,
//        property = {
////                KeycloakOIDCFilter.CONFIG_FILE_PARAM + "=" + "keycloak.json",
////                "keycloak.config.file=keycloak.json",
//                // must  have this annotation
//                "keycloak.config.resolver=PathBasedKeycloakConfigResolver",
//                // handle all role on request to all path
//                "keycloak.securityConstraints[0].authRoles[0]=*",
//                "keycloak.securityConstraints[0].securityCollections[0].patterns[0]=/pantheon/#/*",
//                HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=" + "/pantheon/#/*",
//                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT
//                        + "="
//                        + "(osgi.http.whiteboard.context.name=pantheon)",
//        })
//
//@SlingServletFilter(scope = {SlingServletFilterScope.REQUEST},
//        // this pattern triggers KeycloakFilter to be called: /pantheon/.*
//        pattern = "/pantheon/.*",
//        methods = {"GET","POST"})
//public class KeycloakFilter extends KeycloakOIDCFilter implements Filter {
//
//    private static final Logger log = Logger.getLogger(KeycloakOIDCFilter.class.getName());
//    protected KeycloakDeployment keycloakDeployment;
////    private KeycloakConfigResolver keycloakConfigResolver;
//    private PathBasedKeycloakConfigResolver keycloakConfigResolver;
//    /**
//     * Constructor that can be used to define a {@code KeycloakConfigResolver} that will be used at
//     * initialization to provide the {@code KeycloakDeployment}.
//     *
//     * @param pathBasedKeycloakConfigResolver the resolver
//    */
//    public KeycloakFilter(PathBasedKeycloakConfigResolver pathBasedKeycloakConfigResolver) {
//        this.keycloakConfigResolver = pathBasedKeycloakConfigResolver;
//    }
//
//    public KeycloakFilter() {
//        this(null);
//    }
//
//    @Override
//    public void init(final FilterConfig filterConfig) throws ServletException {
//        // Read the initialization parameters, whitelist (resources that can be accessed without authentication).
//        String skipPatternDefinition = filterConfig.getInitParameter(SKIP_PATTERN_PARAM);
//        if (skipPatternDefinition != null) {
//            skipPattern = Pattern.compile(skipPatternDefinition, Pattern.DOTALL);
//        }
//        // Load client configuration information
//        File file = new File( System.getProperty( "karaf.etc" ) + File.separator + "keycloak.json" );
//        String path = file.getPath();
//        String pathParam = filterConfig.getInitParameter(CONFIG_PATH_PARAM);
//        if (pathParam != null){
//            path = pathParam;
//        }
//        InputStream is = filterConfig.getServletContext().getResourceAsStream(path);
//        keycloakDeployment = createKeycloakDeploymentFrom(is);
//        deploymentContext = new AdapterDeploymentContext(keycloakDeployment);
//        filterConfig
//                .getServletContext()
//                .setAttribute(AdapterDeploymentContext.class.getName(), deploymentContext);
//        nodesRegistrationManagement = new NodesRegistrationManagement();
//    }
//
//    @Override
//    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
//        log.info("[" + KeycloakFilter.class.getSimpleName() + "] Keycloak OIDC Filter");
//        super.doFilter(req, res, chain);
//    }
//
//    private KeycloakDeployment createKeycloakDeploymentFrom(InputStream is) {
//        if (is == null) {
//            log.info("[" +KeycloakFilter.class.getSimpleName() + "] No adapter configuration. Keycloak is unconfigured and will deny all requests.");
//            return new KeycloakDeployment();
//        }
//        return KeycloakDeploymentBuilder.build(is);
//    }
//}
