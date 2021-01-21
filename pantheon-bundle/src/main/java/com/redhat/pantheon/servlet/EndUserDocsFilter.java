package com.redhat.pantheon.servlet;

import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Matcher;

import static com.redhat.pantheon.servlet.ServletUtils.getPathMatcher;

@Component(
        service = Filter.class
)
@SlingServletFilter(
        methods = "GET",
        pattern = EndUserDocsFilter.PATH_PATTERN)
public class EndUserDocsFilter implements Filter {

    static final String PATH_PATTERN = "/pantheon/docs/(?<path>.*)";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // Get the id, everything after the prefix
        Matcher pathMatcher = getPathMatcher(PATH_PATTERN, (HttpServletRequest) request);
        String path = pathMatcher.group("path");
        request.setAttribute("document", path);

        request.getRequestDispatcher("/pantheon/staticdocs").forward(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
