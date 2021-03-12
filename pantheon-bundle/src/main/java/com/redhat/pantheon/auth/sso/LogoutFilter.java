package com.redhat.pantheon.auth.sso;

import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletFilterScope;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A Filter that handles logout request for Keycloak Integration
 *
 * * @author Lisa Davidson
 */
@Component(
        service = Filter.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Filter for keycloak logout request",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletFilter(scope = {SlingServletFilterScope.REQUEST},
        pattern = "/system/sling/logout",
        methods = {"GET"})
public class LogoutFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(LogoutFilter.class.getName());
    private static final String REDIRECT_URI = "/pantheon/";
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException,
            ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        if (System.getenv("AUTH_SERVER_URL") != null) {
            // Invalidate keycloak session
            request.getSession().invalidate();
        }
            chain.doFilter(request, response);
        }

    public void destroy() {
    }
}
