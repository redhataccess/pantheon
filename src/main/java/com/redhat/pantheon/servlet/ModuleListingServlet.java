package com.redhat.pantheon.servlet;

import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.model.module.Module;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;

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
            keyParam = "jcr:created";
        }
        if(!"desc".equals(directionParam)) {
            directionParam = "asc";
        }

        StringBuilder queryBuilder = new StringBuilder()
                .append("select m.* from [pant:module] as m ")
                    .append("INNER JOIN [pant:moduleRevision] as rev ON ISDESCENDANTNODE(rev, m) ")
                .append("where (isdescendantnode(m, '/content/repositories') ")
                    .append("or isdescendantnode(m, '/content/modules') ")
                    .append("or isdescendantnode(m, '/content/sandbox')) ")
                // look in ALL revisions (all locales)
                .append("AND (rev.[metadata/jcr:title] like '%" + searchParam + "%' ")
                    .append("OR rev.[metadata/jcr:description] like " + "'%" + searchParam + "%') ");

        if(!isNullOrEmpty(keyParam) && !isNullOrEmpty(directionParam)) {
            queryBuilder.append(" order by m.[")
                    .append(keyParam).append("] ")
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
        m.put("jcr:title", draftMetadata.isPresent() ? draftMetadata.get().title.get() : releasedMetadata.get().title.get());
        m.put("jcr:description", draftMetadata.isPresent() ? draftMetadata.get().description.get() : releasedMetadata.get().description.get());
        // Assume the path is something like: /content/<something>/my/resource/path
        m.put("pant:transientPath", resourcePath.substring("/content/".length()));
        // Example path: /content/repositories/ben_2019-04-11_16-15-15/shared/attributes.module.adoc
        String[] fragments = resourcePath.split("/");
        // Example fragments: ["", "content", "repositories", "ben_2019-04-11_16-15-15", "shared", "attributes.module.adoc"]
        m.put("pant:transientSource", fragments[2]);
        if (!"modules".equals(fragments[2])) {
            m.put("pant:transientSourceName", fragments[3]);
        }
        for (Map.Entry<String, Object> e : m.entrySet()) {
            log.trace("{} :: {} :: {}", e.getKey(), e.getValue().getClass(), e.getValue());
        }
        return m;
    }
}
