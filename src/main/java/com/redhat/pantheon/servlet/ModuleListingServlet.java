package com.redhat.pantheon.servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
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

        //FIXME - we had "select * from [pant:module]..." here, BUT we were seeing problems that after a very small
        //FIXME - number of module upload/delete operations, this query would suddenly return only a very small number
        //FIXME - of modules. Changing this to [nt:base] seems to fix it, but I don't know why. Perhaps it's some bug
        //FIXME - related to 'nodetypes.cnd' getting reinstalled on every package deployment, resulting in the
        //FIXME - pant:module nodetype being assigned some new internal id, but that's pure speculation.
        StringBuilder queryBuilder = new StringBuilder()
                .append("select * from [nt:base] as a ")
                .append("where [sling:resourceType] = 'pantheon/module' ")
                .append("and (isdescendantnode(a, '/content/repositories') ")
                .append("or isdescendantnode(a, '/content/modules') ")
                .append("or isdescendantnode(a, '/content/sandbox')) ")
                .append("AND (a.[jcr:title] like '%" + searchParam + "%' ")
                .append("OR a.[jcr:description] like " + "'%" + searchParam + "%')");

        if(!isNullOrEmpty(keyParam) && !isNullOrEmpty(directionParam)) {
            queryBuilder.append(" order by a.[")
                    .append(keyParam).append("] ")
                    .append(directionParam);
        }

        return queryBuilder.toString();
    }

    @Override
    protected Map<String, Object> resourceToMap(Resource resource) {
        Map<String, Object> m = super.resourceToMap(resource);
        String resourcePath = resource.getPath();
        m.put("name", resource.getName());
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
