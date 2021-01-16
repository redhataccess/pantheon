package com.redhat.pantheon.auth.keycloak;

import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletFilterScope;
import org.keycloak.adapters.*;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import javax.servlet.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Component(
        immediate = true,
        service = Filter.class,
        property = {
//                KeycloakOIDCFilter.CONFIG_FILE_PARAM + "=" + "keycloak.json",
                "keycloak.config.file=keycloak.json",
                // must  have this annotation
//                "keycloak.config.resolver=PathBasedKeycloakConfigResolver",
                // handle all role on request to all path
                "keycloak.securityConstraints[0].authRoles[0]=*",
                "keycloak.securityConstraints[0].securityCollections[0].patterns[0]=/*",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=" + "/*",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT
                        + "="
                        + "(osgi.http.whiteboard.context.name=pantheon)",
        })

@SlingServletFilter(scope = {SlingServletFilterScope.REQUEST},
        // this pattern triggers KeycloakFilter to be called: /pantheon/.*
        pattern = "/pantheon/.*",
        methods = {"GET","POST"})
public class KeycloakFilter extends KeycloakOIDCFilter implements Filter {

    private static final Logger log = Logger.getLogger(KeycloakOIDCFilter.class.getName());
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
        String skipPatternDefinition = filterConfig.getInitParameter(SKIP_PATTERN_PARAM);
        if (skipPatternDefinition != null) {
            skipPattern = Pattern.compile(skipPatternDefinition, Pattern.DOTALL);
        }

        if (keycloakConfigResolver != null) {
            deploymentContext = new AdapterDeploymentContext(keycloakConfigResolver);
            log.log(
                    Level.INFO,
                    "Using {0} to resolve Keycloak configuration on a per-request basis.",
                    keycloakConfigResolver.getClass());
        } else {
            String configResolverClass = filterConfig.getInitParameter(CONFIG_RESOLVER_PARAM);
            if (configResolverClass != null) {
                try {
                    KeycloakConfigResolver configResolver =
                            (KeycloakConfigResolver)
                                    getClass().getClassLoader().loadClass(configResolverClass).newInstance();
                    deploymentContext = new AdapterDeploymentContext(configResolver);
                    log.log(
                            Level.INFO,
                            "Using {0} to resolve Keycloak configuration on a per-request basis.",
                            configResolverClass);
                } catch (Exception ex) {
                    log.log(
                            Level.FINE,
                            "The specified resolver {0} could NOT be loaded. Keycloak is unconfigured and will deny all requests. Reason: {1}",
                            new Object[] {configResolverClass, ex.getMessage()});
                    deploymentContext = new AdapterDeploymentContext(new KeycloakDeployment());
                }
            } else {
                String fp = filterConfig.getInitParameter(CONFIG_FILE_PARAM);
                InputStream is = null;
                if (fp != null) {
                    Path configPath = Paths.get(fp).toAbsolutePath();
                    try {
                        is = new FileInputStream(configPath.toFile());
                    } catch (FileNotFoundException e) {
                        log.log(Level.WARNING, "Could not load config file {0}", configPath);
                        throw new RuntimeException(e);
                    }
                } else {
//                    String path = "keycloak.json";
                    File file = new File( System.getProperty( "karaf.etc" ) + File.separator + "keycloak.json" );
                    String path = file.getPath();
                    String pathParam = filterConfig.getInitParameter(CONFIG_PATH_PARAM);
                    if (pathParam != null) {
                        path = pathParam;
                    }
                    is = filterConfig.getServletContext().getResourceAsStream(path);
                }
                KeycloakDeployment kd = createKeycloakDeploymentFrom(is);
                deploymentContext = new AdapterDeploymentContext(kd);
                log.info("[" + KeycloakFilter.class.getSimpleName() + "] Keycloak is using a per-deployment configuration.");
            }
        }
        filterConfig
                .getServletContext()
                .setAttribute(AdapterDeploymentContext.class.getName(), deploymentContext);
        nodesRegistrationManagement = new NodesRegistrationManagement();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        // Ensure keycloakDeployment is created
//        FilterConfig filterConfig = new FilterConfig() {
//            @Override
//            public String getFilterName() {
//                return KeycloakFilter.class.getName();
//            }
//
//            @Override
//            public ServletContext getServletContext() {
//                return req.getServletContext();
//            }
//
//            @Override
//            public String getInitParameter(String s) {
//                return s;
//            }
//
//            @Override
//            public Enumeration<String> getInitParameterNames() {
//                return null;
//            }
//        };
//        if (keycloakConfigResolver != null) {
//            deploymentContext = new AdapterDeploymentContext(keycloakConfigResolver);
//            log.log(
//                    Level.INFO,
//                    "Using {0} to resolve Keycloak configuration on a per-request basis.",
//                    keycloakConfigResolver.getClass());
//        } else {
//            String configResolverClass = filterConfig.getInitParameter(CONFIG_RESOLVER_PARAM);
//            if (configResolverClass != null) {
//                try {
//                    KeycloakConfigResolver configResolver =
//                            (KeycloakConfigResolver)
//                                    getClass().getClassLoader().loadClass(configResolverClass).newInstance();
//                    deploymentContext = new AdapterDeploymentContext(configResolver);
//                    log.log(
//                            Level.INFO,
//                            "Using {0} to resolve Keycloak configuration on a per-request basis.",
//                            configResolverClass);
//                } catch (Exception ex) {
//                    log.log(
//                            Level.FINE,
//                            "The specified resolver {0} could NOT be loaded. Keycloak is unconfigured and will deny all requests. Reason: {1}",
//                            new Object[] {configResolverClass, ex.getMessage()});
//                    deploymentContext = new AdapterDeploymentContext(new KeycloakDeployment());
//                }
//            } else {
//                String fp = filterConfig.getInitParameter(CONFIG_FILE_PARAM);
//                InputStream is = null;
//                if (fp != null) {
//                    Path configPath = Paths.get(fp).toAbsolutePath();
//                    try {
//                        is = new FileInputStream(configPath.toFile());
//                    } catch (FileNotFoundException e) {
//                        log.log(Level.WARNING, "Could not load config file {0}", configPath);
//                        throw new RuntimeException(e);
//                    }
//                } else {
//                    String path = "/content/keycloak/keycloak.json";
//                    String pathParam = filterConfig.getInitParameter(CONFIG_PATH_PARAM);
//                    if (pathParam != null) {
//                        path = pathParam;
//                    }
//                    is = filterConfig.getServletContext().getResourceAsStream(path);
//                }
//                KeycloakDeployment kd = createKeycloakDeploymentFrom(is);
//                deploymentContext = new AdapterDeploymentContext(kd);
//                log.info("[" + KeycloakFilter.class.getSimpleName() + "] Keycloak is using a per-deployment configuration.");
//            }
//        }
//        filterConfig
//                .getServletContext()
//                .setAttribute(AdapterDeploymentContext.class.getName(), deploymentContext);
//        nodesRegistrationManagement = new NodesRegistrationManagement();
        log.info("[" + KeycloakFilter.class.getSimpleName() + "] Keycloak OIDC Filter");
        super.doFilter(req, res, chain);
    }

    private KeycloakDeployment createKeycloakDeploymentFrom(InputStream is) {
        if (is == null) {
            log.info("No adapter configuration. Keycloak is unconfigured and will deny all requests.");
            return new KeycloakDeployment();
        }
        return KeycloakDeploymentBuilder.build(is);
    }
}
