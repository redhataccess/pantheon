package com.redhat.pantheon.servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletFilterScope;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

@Component(
        service = Filter.class
)
@SlingServletFilter(scope = {SlingServletFilterScope.REQUEST},
        pattern = "/.*",
        methods = {"GET","HEAD","OPTIONS"})
public class TestFilter implements Filter {
    private final Logger log = LoggerFactory.getLogger(TestFilter.class);
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        log.info("[" + TestFilter.class.getSimpleName() + "] request: " + ((SlingHttpServletRequest) request).getResource().getPath());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
