package com.redhat.pantheon.servlet.onetime;

import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.module.ModuleVersion;
import com.redhat.pantheon.servlet.ServletUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=One-time servlet to add ackStatus nodes to existing legacy moduleVersions",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/onetime/ackStatusCreate")
public class AckStatusCreateServlet extends SlingSafeMethodsServlet {

    private static final String QUERY = "select * from [pant:moduleVersion] as a " +
            "where [ackStatus/sling:resourceType] is null and isdescendantnode(a, '/content/repositories')";

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        @NotNull ResourceResolver resolver = request.getResourceResolver();
        JcrQueryHelper queryHelper = new JcrQueryHelper(resolver);
        try {
            Stream<Resource> results = queryHelper.query(QUERY);

            long count = results.map(r -> r.adaptTo(ModuleVersion.class))
                    .peek(mv -> mv.ackStatus().getOrCreate())
                    .count();

            Map<String, Object> m = new HashMap<>();
            m.put("count", count);

            resolver.commit();
            ServletUtils.writeAsJson(response, m);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

    }
}
