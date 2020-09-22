package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.servlet.AbstractJsonSingleQueryServlet;
import com.redhat.pantheon.servlet.util.ServletHelper;
import com.redhat.pantheon.servlet.util.SlingPathSuffix;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsLocale;
import static com.redhat.pantheon.servlet.ServletUtils.writeAsJson;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Get operation to render a Module's related assembly list in JSON format.
 *
 * The url to GET a request from the server is /api/module/assemblies
 * Example: <server_url>/module/assemblies.json/b537ef3c-5c7d-4280-91ce-e7e818e6cc11
 *
 * @author A.P. Rajshekhar
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet to facilitate GET operation which accepts the module variant uuid to output module assemblies",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/module/assemblies")
public class IncludedInAssembliesServlet extends AbstractJsonSingleQueryServlet {

    private final Logger log = LoggerFactory.getLogger(IncludedInAssembliesServlet.class);
    private final SlingPathSuffix suffix = new SlingPathSuffix("/{variantUuid}");

    @Override
    protected String getQuery(SlingHttpServletRequest request) {
        Map<String, String> parameters = suffix.getParameters(request);
        String uuid = parameters.get("variantUuid");
        // Hydra fetch calls look like this:
        // Calling pantheon2 with url https://<HOST>/module/assemblies.json/b537ef3c-5c7d-4280-91ce-e7e818e6cc11&proxyHost=<SOMEHOST>&proxyPort=8080&throwExceptionOnFailure=false
        StringBuilder query = new StringBuilder("select * from [pant:moduleVariant] as moduleVariant WHERE moduleVariant.[jcr:uuid] = '")
                .append(ServletHelper.sanitizeSuffix(uuid))
                .append("'");
        return query.toString();
    }

    @Override
    protected Map<String, Object> resourceToMap(@NotNull SlingHttpServletRequest request, @NotNull Resource resource) throws RepositoryException {
        ModuleVariant moduleVariant = resource.adaptTo(ModuleVariant.class);
        Map<String, Object> details = new HashMap<>();
        List<HashMap<String, String>>includeAssemblies = new ArrayList<>();
        ServletHelper.addAssemblyDetails(ServletHelper.getModuleUuidFromVariant(moduleVariant),
                includeAssemblies, request, true, true);
        details.put("assemblies",includeAssemblies);
        details.put("status","200");
        return details;
    }
}
