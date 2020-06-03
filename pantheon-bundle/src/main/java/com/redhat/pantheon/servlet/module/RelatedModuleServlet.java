package com.redhat.pantheon.servlet.module;

import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.module.Module;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsLocale;
import static com.redhat.pantheon.servlet.ServletUtils.writeAsJson;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Get operation to render a Released Module's related module list in JSON format.
 * Only two parameters are expected in the Get request:
 * 1. locale - Optional; indicates the locale that the module content is in, defaulted to en-US
 * 2. module_id - indicates the uuid string which uniquely identifies a module
 *
 * The url to GET a request from the server is /api/module/related
 * Example: <server_url>/api/module/related?locale=en-us&module_id=xyz
 * The said url is accessible outside of the system without any authentication.
 *
 * @author Ben Radey
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet to facilitate GET operation which accepts locale and module uuid to output module relationships",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/api/module/related")
public class RelatedModuleServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(RelatedModuleServlet.class);

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        Locale locale = paramValueAsLocale(request, "locale", DEFAULT_MODULE_LOCALE);
        String uuidParam = paramValue(request, "module_id", "");

        StringBuilder query = new StringBuilder("select * from [pant:module] as module WHERE module.[jcr:uuid] = '")
                .append(uuidParam)
                .append("'");

        JcrQueryHelper queryHelper = new JcrQueryHelper(request.getResourceResolver());
        try {
            Stream<Resource> resultStream = queryHelper.query(query.toString());

            Optional<Resource> firstResource = resultStream.findFirst();
            if(!firstResource.isPresent()) {
                response.sendError(SC_NOT_FOUND, "Requested content not found.");
            }

            resultStream = queryHelper.query("select * from [pant:module] as module", 3, 0);

            List<Map> related = new ArrayList<>();
            resultStream.map(r -> r.adaptTo(Module.class))
                    .forEach(module -> {
                            Map<String, String> m = new HashMap<>();
                            m.put("title", module.moduleLocale(locale).get()
                                    .variants().get()
                                    .defaultVariant().get()
                                    .released().get()
                                    .metadata().get()
                                    .title().get());
                            m.put("url", "https://www.redhat.com/moduleplaceholder");
                            m.put("uuid", module.uuid().get());
                        related.add(m);
            });

            Map result = new HashMap();
            result.put("related", related);

            writeAsJson(response, result);
        } catch (RepositoryException e) {
            throw new ServletException(e);
        }
    }
}
