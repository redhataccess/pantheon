package com.redhat.pantheon.servlet;

import com.redhat.pantheon.helper.Symlinks;
import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.servlet.assets.ImageServletFilter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static com.redhat.pantheon.servlet.ServletUtils.getPathMatcher;


@Component(
        service = Filter.class
)
@SlingServletFilter(
        methods = "GET",
        pattern = DocumentPreviewFilter.PATH_PATTERN)
public class DocumentPreviewFilter implements Filter {

    static final String PATH_PATTERN = "/pantheon/preview/(?<documentId>.*)";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("Servlet called :)");
        // Get the id, everything after the prefix
        Matcher pathMatcher = getPathMatcher(PATH_PATTERN, (HttpServletRequest) request);
        String docId = pathMatcher.group("documentId");

        String query = "select * from [pant:document] as document WHERE document.[jcr:uuid] = '" + docId + "'";
        @NotNull ResourceResolver resolver = ((SlingHttpServletRequest) request).getResourceResolver();
        JcrQueryHelper queryHelper = new JcrQueryHelper(resolver);
        try {
            Stream<Resource> resultStream = queryHelper.query(query);
            Optional<Resource> firstResource = resultStream.findFirst();
            if (firstResource.isPresent()) {
                Resource res = firstResource.get();
                request.getRequestDispatcher(res.getPath() + ".preview").forward(request, response);
            } else {
                // Not a document, but maybe a document variant
                String query2 = "select * from [pant:documentVariant] as variant WHERE variant.[jcr:uuid] = '" + docId + "'";
                resultStream = queryHelper.query(query2);
                firstResource = resultStream.findFirst();
                if (firstResource.isPresent()) {
                    Resource res = firstResource.get();
                    DocumentVariant dv = res.adaptTo(DocumentVariant.class);
                    request.getRequestDispatcher(dv.getPath() + ".preview").forward(request, response);
                }
            }
        } catch (RepositoryException e) {

        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
