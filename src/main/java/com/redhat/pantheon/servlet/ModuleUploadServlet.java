package com.redhat.pantheon.servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ben on 7/1/19.
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which accepts module uploads and versions them appropriately",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletResourceTypes(
        resourceTypes="pantheon/module",
        methods= "POST")
public class ModuleUploadServlet extends SlingAllMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ModuleUploadServlet.class);

    private String getContent(SlingHttpServletRequest request, String requestParameter) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getRequestParameter(requestParameter).getInputStream()));

        StringBuilder contentBuilder = new StringBuilder();
        reader.lines().forEachOrdered(line -> contentBuilder.append(line).append(System.getProperty("line.separator")));

        return contentBuilder.toString();
    }

    private boolean contentMatches(Resource moduleMaster, String locale, String content) {
        Resource moduleLocalization = moduleMaster.getChild(locale);
        String latestVersion = moduleLocalization.getValueMap().get("latestVersion", String.class);

        //Get current version and compare content
        String currentContent = moduleLocalization.getValueMap().get("v" + latestVersion + "/asciidoc/jcr:content/jcr:data", String.class);
        return currentContent.equals(content);
    }

    private Resource storeNewVersion(ResourceResolver resolver, Resource moduleMaster, String locale, String content,
                                     String contentType) throws PersistenceException {
        Resource moduleLocalization = moduleMaster.getChild(locale);
        String latestVersion = moduleLocalization.getValueMap().get("latestVersion", String.class);

        long nextVersion = Long.valueOf(latestVersion) + 1;
        Map<String, Object> versionProps = new HashMap<>();
        versionProps.put("jcr:primaryType", "pant:moduleVersion");
        versionProps.put("version", nextVersion);
        Resource newVersion = resolver.create(moduleLocalization, "v" + nextVersion, versionProps);
        moduleLocalization.adaptTo(ModifiableValueMap.class).put("latestVersion", nextVersion);

        Map<String, Object> asciidocProps = new HashMap<>();
        asciidocProps.put("jcr:primaryType", "nt:file");
        Resource asciidoc = resolver.create(newVersion, "asciidoc", asciidocProps);

        Map<String, Object> contentProps = new HashMap<>();
        contentProps.put("jcr:primaryType", "nt:resource");
        contentProps.put("jcr:mimeType", contentType);
        contentProps.put("jcr:data", content);
        resolver.create(asciidoc, "jcr:content", contentProps);

        resolver.commit();

        return newVersion;
    }

    @Override
    protected void doPost(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        try {
            Resource moduleMaster = request.getResource();
            log.info("ModuleUploadServlet handling request for {}", moduleMaster.getPath());
            // Use en_US if nothing is specified in the request.
            String locale = request.getParameter("locale");
            if (locale == null || locale.isEmpty()) {
                log.info("Locale not set via request.");
                locale = new Locale("en", "US").toString();
            }
            log.info("Locale set to: " + locale);

            String contentType = request.getRequestParameter(locale+"/v1/asciidoc").getContentType();
            String content = getContent(request, locale+"/v1/asciidoc");

            if (contentMatches(moduleMaster, locale, content)) {
                log.debug("New and old content match, doing nothing.");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                log.debug("New content differs from old content, creating new version...");
                Resource newVersion = storeNewVersion(request.getResourceResolver(), moduleMaster, locale, content, contentType);
                log.debug("Created new version at " + newVersion.getPath());
                response.setStatus(HttpServletResponse.SC_CREATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw e;
        }
    }
}
