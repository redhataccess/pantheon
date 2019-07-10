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
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
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

        long lOffset = offset == null ? 0 : Long.valueOf(offset);
        long pageLimit = limit == null ? Long.MAX_VALUE : Long.valueOf(limit);

        long lLimit = pageLimit == Long.MAX_VALUE ? pageLimit : pageLimit + 1;

        try {
            List<Map<String, Object>> resultSet = mdr.getModulesSort(searchParam, keyParam, directionParam, lOffset, lLimit);

            List<Map<String, Object>> data = new ArrayList<>(resultSet);
            boolean hasNextPage = false;
            if (data.size() > pageLimit) {
                data.remove(data.size() - 1); //Removing the +1 element that we added for the next page
                hasNextPage = true;
            }
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("hasNextPage", hasNextPage);
            payload.put("data", data);
            writeAsJson(response, payload);
        } catch (RepositoryException e) {
            log.error("/modules.json error", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
