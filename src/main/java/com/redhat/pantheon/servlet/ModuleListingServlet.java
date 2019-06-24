package com.redhat.pantheon.servlet;

import com.redhat.pantheon.data.ModuleDataRetriever;
import org.apache.http.HttpStatus;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.writeAsJson;

/**
 * Created by ben on 4/18/19.
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which provides initial module listing and search functionality",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/modules.json")
public class ModuleListingServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ModuleListingServlet.class);

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        ModuleDataRetriever mdr = new ModuleDataRetriever(request.getResourceResolver());
        String searchParam = paramValue(request, "search");
        String keyParam = paramValue(request, "key");
        String directionParam = paramValue(request, "direction");
        String offset = paramValue(request, "offset");
        String limit = paramValue(request, "limit");

        try {
            List<Map<String, Object>> payload = mdr.getModulesSort(searchParam, keyParam, directionParam, offset, limit);
            writeAsJson(response, payload);
        } catch (RepositoryException e) {
            log.error("/modules.json error", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
