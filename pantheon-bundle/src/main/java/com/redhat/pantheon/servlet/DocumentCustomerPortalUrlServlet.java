package com.redhat.pantheon.servlet;

import com.redhat.pantheon.extension.url.CustomerPortalUrlUuidProvider;
import com.redhat.pantheon.extension.url.UrlException;
import com.redhat.pantheon.extension.url.UrlProvider;
import com.redhat.pantheon.model.ModelException;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.servlet.util.ServletHelper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which provides a Customer Portal URL for any document",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletResourceTypes(
        resourceTypes = { "pantheon/moduleVariant", "pantheon/assemblyVariant", "pantheon/module", "pantheon/assembly" },
        methods = "GET",
        selectors = "url",
        extensions = "json")
public class DocumentCustomerPortalUrlServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        Resource r = request.getResource();
        String url = "";
        String type = "";
        try {
            Object o = ServletHelper.resourceToModel(r);
            DocumentVariant dv = o instanceof DocumentVariant
                    ? (DocumentVariant) o
                    : ((Document) o).locale("en_US").get()
                    .variants().get()
                    .canonicalVariant().get();
            UrlProvider provider = new CustomerPortalUrlUuidProvider(dv);
            url = provider.generateUrlString();
            type = provider.getUrlType().toString();
        } catch (ModelException | UrlException e) {
            url = e.getMessage();
            type = "ERROR";
        }
        Map<String, String> map = new HashMap<>();
        map.put("url", url);
        map.put("type", type);
        ServletUtils.writeAsJson(response, map);
    }
}
