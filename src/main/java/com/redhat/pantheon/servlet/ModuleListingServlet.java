package com.redhat.pantheon.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.pantheon.data.ModuleDataRetriever;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

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

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        ModuleDataRetriever mdr = new ModuleDataRetriever(request.getResourceResolver());
        String searchParam = getParam(request, "search");
        String keyParam = getParam(request, "key");
        String directionParam = getParam(request, "direction");
        String offset = getParam(request, "offset");
        String count = getParam(request, "count");

        List<Map<String, Object>> payload = mdr.getModulesSort(searchParam, keyParam, directionParam);

        response.setContentType("application/json");
        Writer w = response.getWriter();
        w.write(new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(payload));
    }

    private String getParam(SlingHttpServletRequest request, String param) {
        String ret = "";
        if (request.getParameterMap().containsKey(param)) {
            ret = request.getRequestParameter(param).toString();
        }
        return ret;
    }
}
