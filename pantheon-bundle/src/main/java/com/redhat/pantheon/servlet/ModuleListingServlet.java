package com.redhat.pantheon.servlet;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;

import java.util.Map;
import java.util.Optional;

import javax.servlet.Servlet;

import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.model.module.Module;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ModuleListingServlet extends AbstractJsonQueryServlet {

    private final Logger log = LoggerFactory.getLogger(ModuleListingServlet.class);

    @Override
    protected String getQuery(SlingHttpServletRequest request) {
        String searchParam = paramValue(request, "search", "");
        String keyParam = paramValue(request, "key");
        String directionParam = paramValue(request, "direction");
        String offset = paramValue(request, "offset");
        String limit = paramValue(request, "limit");

        if(!newArrayList("jcr:title", "jcr:description").contains(keyParam)) {
            keyParam = "pant:dateUploaded";
        }
        if(!"desc".equals(directionParam)) {
            directionParam = "asc";
        }

        // FIXME Searching by resourceType because in some cases, searching directly on the primaryType
        // is not returning any results
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT m.* from [nt:base] AS m ")
                .append("LEFT OUTER JOIN [nt:base] AS loc ON  ISCHILDNODE(loc, m) ")
                .append("LEFT OUTER JOIN [nt:base] AS draft ON  draft.[jcr:uuid] = loc.[draft] ")
                .append("LEFT OUTER JOIN [nt:base] AS release ON  release.[jcr:uuid] = loc.[released] ")
                .append("WHERE m.[jcr:primaryType] = 'pant:module' ")
                .append("AND loc.[jcr:primaryType] = 'pant:moduleLocale' ")
                .append("AND (draft.[jcr:primaryType] = 'pant:moduleVersion' OR draft.[jcr:primaryType] IS NULL) ")
                .append("AND (release.[jcr:primaryType] = 'pant:moduleVersion' OR release.[jcr:primaryType] IS NULL) ")
                .append("AND (draft.[metadata/jcr:title] LIKE '%" + searchParam + "%' ")
                    .append("OR draft.[metadata/jcr:description] LIKE '%" + searchParam + "%' ")
                    .append("OR release.[metadata/jcr:title] LIKE '%" + searchParam + "%' ")
                    .append("OR release.[metadata/jcr:description] LIKE '%" + searchParam + "%')");

        if(!isNullOrEmpty(keyParam) && !isNullOrEmpty(directionParam)) {
            queryBuilder.append(" ORDER BY coalesce(draft.[metadata/")
                    .append(keyParam).append("],release.[metadata/")
                    .append(keyParam).append("]) ")
                    .append(directionParam);
        }

        return queryBuilder.toString();
    }

    @Override
    protected Map<String, Object> resourceToMap(Resource resource) {
        Module module = resource.adaptTo(Module.class);
        Optional<Metadata> draftMetadata = module.getDraftMetadata(DEFAULT_MODULE_LOCALE);
        Optional<Metadata> releasedMetadata = module.getReleasedMetadata(DEFAULT_MODULE_LOCALE);

        // TODO Need some DTOs to convert to maps
        Map<String, Object> m = super.resourceToMap(resource);
        String resourcePath = resource.getPath();
        m.put("name", resource.getName());
        // TODO need to provide both released and draft to the api caller
        m.put("pant:dateUploaded", draftMetadata.isPresent() ? draftMetadata.get().dateUploaded().get() : releasedMetadata.get().dateUploaded().get());
        m.put("jcr:title", draftMetadata.isPresent() ? draftMetadata.get().title().get() : releasedMetadata.get().title().get());
        m.put("jcr:description", draftMetadata.isPresent() ? draftMetadata.get().description().get() : releasedMetadata.get().description().get());
        // Assume the path is something like: /content/<something>/my/resource/path
        m.put("pant:transientPath", resourcePath.substring("/content/".length()));
        // Example path: /content/repositories/ben_2019-04-11_16-15-15/shared/attributes.module.adoc
        String[] fragments = resourcePath.split("/");
        // Example fragments: ["", "content", "repositories", "ben_2019-04-11_16-15-15", "shared", "attributes.module.adoc"]
        m.put("pant:transientSource", fragments[2]);
        if (!"modules".equals(fragments[2])) {
            m.put("pant:transientSourceName", fragments[3]);
        }

        log.trace(m.toString());
        return m;
    }
}
