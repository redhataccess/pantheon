package com.redhat.pantheon.auth.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.pantheon.auth.keycloak.utils.HttpClientUtil;
import com.redhat.pantheon.auth.keycloak.utils.HttpResponse;
import org.apache.http.Header;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletFilterScope;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Component(
        service = Filter.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Filter to enable Keycloak Single Sign On support",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=" + "/pantheon/*",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT
                        + "="
                        + "(osgi.http.whiteboard.context.name=pantheon)",
        })
@SlingServletFilter(scope = {SlingServletFilterScope.REQUEST},
        pattern = "/pantheon/.*",
        methods = {"GET","POST"})
public class KeycloakOIDCFilter implements Filter {

    private final static Logger log = Logger.getLogger("" + KeycloakOIDCFilter.class);
    public static final String SKIP_PATTERN_PARAM = "keycloak.config.skipPattern";
    public static final String CONFIG_PATH_PARAM = "keycloak.config.path";
    protected KeycloakDeployment keycloakDeployment;
    private PathBasedKeycloakConfigResolver keycloakConfigResolver;
    protected Pattern skipPattern;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // Read the initialization parameters, whitelist (resources that can be accessed without authentication).
        String skipPatternDefinition = filterConfig.getInitParameter(SKIP_PATTERN_PARAM);
        if (skipPatternDefinition != null) {
            skipPattern = Pattern.compile(skipPatternDefinition, Pattern.DOTALL);
        }
        // Load client configuration information
//        String path = "keycloak.json";
        File file = new File( System.getProperty( "karaf.etc" ) + File.separator + "keycloak.json" );
        String path = file.getPath();
        String pathParam = filterConfig.getInitParameter(CONFIG_PATH_PARAM);
        if (pathParam != null){
            path = pathParam;
        }
        InputStream is = filterConfig.getServletContext().getResourceAsStream(path);
        keycloakDeployment = createKeycloakDeploymentFrom(is);
    }

    private KeycloakDeployment createKeycloakDeploymentFrom(InputStream is) {
        if (is == null) {
            log.info("No adapter configuration. "
                    + "Keycloak is unconfigured and will deny all requests.");
            return new KeycloakDeployment();
        }
        return KeycloakDeploymentBuilder.build(is);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.info("[" + KeycloakOIDCFilter.class.getSimpleName() + "] createKeycloakDeployeFrom InputSteam");
        File file = new File( System.getProperty( "karaf.etc" ) + File.separator + "keycloak.json" );
        // load config from the file system
        InputStream is = new FileInputStream(file);
        keycloakDeployment = createKeycloakDeploymentFrom(is);

        log.info("Keycloak OIDC Filter");
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        //Whitelist URL, direct release
        if (shouldSkip(request)) {
            chain.doFilter(req, res);
            return;
        }
        //Not whitelist, and not logged in
        HttpSession  session = request.getSession();
        if(session.getAttribute("access_token_str") == null){
            String code = request.getParameter("code");
            // If there is no authorization code, then turn to keycloak
            if(code == null){
                String authURL = this.getRedirectURL(request);
                log.info("turn to keycloak authentication");
                log.info(authURL);
                response.sendRedirect(authURL);
                return;
            }else{
                // The request carries an authorization code
                String sessioncode = (String)session.getAttribute("code");
                if(code.equals(sessioncode)){
                    String authURL = this.getRedirectURL(request);
                    log.info ("Authorization code has been used, re-directed to keycloak authentication");
                    log.info(authURL);
                    response.sendRedirect(authURL);
                    return;
                }
                session.setAttribute("code", code);
                log.info("return URL:"+request.getRequestURL().toString()+request.getQueryString());
                log.info("Get authorization code:"+code);
                // There is an authorization code, then exchange the token according to the authorization code
                String url = keycloakDeployment.getAuthServerBaseUrl()
                        + "/realms/"
                        + keycloakDeployment.getRealm()
                        + "/protocol/openid-connect/token";
                Map<String, String> params = new HashMap<String, String>();
                params.put("code", code);
                params.put("grant_type", "authorization_code");
                params.put("client_id", keycloakDeployment.getResourceName());
                params.put("redirect_uri", getBaseURL(request));
                params.put("client_secret", String.valueOf(keycloakDeployment.getResourceCredentials().get("secret")));
                log.info ("authorization code in exchange for token");
                log.info(url);
                log.info(params.toString());
                Map<String,String> requestHeaders = new HashMap<String, String>();
                requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");
                HttpResponse result = HttpClientUtil.httpPostForm(url, params, requestHeaders, "UTF-8");
                if (result.getStatusCode() != 200) {
                    log.info(result.getStatusCode() + "");
                    Header[] headers = result.getHeaders();
                    for (int i = 0; i < headers.length; i++) {
                        log.info(headers[i].getName() + "  "
                                + headers[i].getValue());
                    }
                    log.info("------------body--------------");
                    log.info(result.getBody());
                    throw new RuntimeException("Error in token exchange for authorization code!"
                            + result.getStatusCode() + result.getReasonPhrase());
                }
                Map token = new ObjectMapper().readValue(result.getBody(), Map.class);
                String refreshTokenString = token.get("refresh_token").toString();
                String tokenString = token.get("access_token").toString();
                log.info("--get token-----");
                log.info(tokenString);
                byte[] bytes = Base64.getDecoder().decode(tokenString.split("\\.")[1]);
                String json = new String(bytes);
                Map accessTokenObj = new ObjectMapper().readValue(json, Map.class);
                session.setAttribute("access_token", accessTokenObj);
                session.setAttribute("access_token_str", tokenString);
                session.setAttribute("refresh_token_str", refreshTokenString);
                chain.doFilter(req, res);
                return;
            }
        }else{
            // has logged in, check the token expiration time
            Map accessToken = (Map)session.getAttribute("access_token");
            //unit seconds
            Long exp = Long.parseLong(String.valueOf(accessToken.get("exp")));
            Long now = System.currentTimeMillis()/1000;

            // has expired or less than 5 minutes from the expiration time, then refresh the token.
            if(now > exp || (exp - now) < 5*60){
                log.info("now="+now+",exp="+exp);
                String url = keycloakDeployment.getAuthServerBaseUrl()
                        + "/realms/"
                        + keycloakDeployment.getRealm()
                        + "/protocol/openid-connect/token";
                Map<String, String> params = new HashMap<String, String>();
                params.put("refresh_token", String.valueOf(session.getAttribute("refresh_token_str")));
                params.put("grant_type", "refresh_token");
                params.put("client_id", keycloakDeployment.getResourceName());
                params.put("client_secret", String.valueOf(keycloakDeployment.getResourceCredentials().get("secret")));
                HttpResponse result = HttpClientUtil.httpPostForm(url, params, null, "UTF-8");
                if(result.getStatusCode() != 200){
                    log.info("Refresh token error!"+result.getStatusCode()+ result.getReasonPhrase());
                    String authURL = this.getRedirectURL(request);
                    log.info("---refresh token error, re-authentication----");
                    log.info(authURL);
                    response.sendRedirect(authURL);
                    return;
                }
                Map token = new ObjectMapper().readValue(result.getBody(), Map.class);
                String refreshTokenString = token.get("refresh_token").toString();
                String tokenString = token.get("access_token").toString();
                log.info("---Get the refreshed token---");
                log.info(tokenString);
                byte[] bytes = Base64.getDecoder().decode(tokenString.split("\\.")[1]);
                String json = new String(bytes);
                Map accessTokenObj = new ObjectMapper().readValue(json, Map.class);
                session.setAttribute("access_token", accessTokenObj);
                session.setAttribute("access_token_str", tokenString);
                session.setAttribute("refresh_token_str", refreshTokenString);
                chain.doFilter(req, res);
                return;
            }
            //token is normal, requesting to pass normally.
            chain.doFilter(req, res);
            return;
        }
    }

    private String getRedirectURL(HttpServletRequest request)
            throws UnsupportedEncodingException{
        String callbackURL = URLEncoder.encode(getBaseURL(request), "UTF-8");
        String authURL = keycloakDeployment.getAuthServerBaseUrl()
                + "/realms/"
                + keycloakDeployment.getRealm()
                + "/protocol/openid-connect/auth?client_id="
                + keycloakDeployment.getResourceName()
                + "&state="
                + UUID.randomUUID().toString()
                + "&response_type=code"
                + "&scope=openid"
                + "&redirect_uri="
                + callbackURL;
        return authURL;
    }

    private static String getBaseURL(HttpServletRequest request) {
        String url = request.getScheme()
                + "://"
                + request.getServerName()
                + ":"
                + request.getServerPort()
                + request.getContextPath();
        return url;
    }

    private boolean shouldSkip(HttpServletRequest request) {
        if (skipPattern == null) {
            return false;
        }
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        return skipPattern.matcher(requestPath).matches();
    }

    @Override
    public void destroy() {
    }
}
