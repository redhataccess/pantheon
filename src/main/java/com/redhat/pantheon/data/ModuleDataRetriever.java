package com.redhat.pantheon.data;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ben on 4/18/19.
 */
public class ModuleDataRetriever {

    private static final int PATH_SUBSTRING_INDEX = "/content/".length();

    private final Logger log = LoggerFactory.getLogger(ModuleDataRetriever.class);
    private ResourceResolver resolver;

    public ModuleDataRetriever(ResourceResolver resolver) {
        this.resolver = resolver;
    }

    public List<Map<String, Object>> getModulesNameSort(String searchTerm) {
        return getModules(searchTerm, "order by a.[jcr:title] asc");
    }

    public List<Map<String, Object>> getModulesCreateSort(String searchTerm) {
        return getModules(searchTerm, "order by a.[jcr:created] desc");
    }

    private List<Map<String, Object>> getModules(String query, String querySuffix) {
        if (query.equals("") || query.equals("*") || query == null) {
            query = "";
        } else {
            query = "AND (a.[jcr:title] like "+ "'%"+query+"%' "+
                    "OR a.[jcr:description] like "+ "'%"+query+"%') ";
        }

        //FIXME - we had "select * from [pant:module]..." here, BUT we were seeing problems that after a very small
        //FIXME - number of module upload/delete operations, this query would suddenly return only a very small number
        //FIXME - of modules. Changing this to [nt:base] seems to fix it, but I don't know why. Perhaps it's some bug
        //FIXME - related to 'nodetypes.cnd' getting reinstalled on every package deployment, resulting in the
        //FIXME - pant:module nodetype being assigned some new internal id, but that's pure speculation.
        Iterator<Resource> resources = resolver.findResources("select * from [nt:base] as a " +
                "where [sling:resourceType] = 'pantheon/modules' " +
                "and (isdescendantnode(a, '/content/repositories') " +
                "or isdescendantnode(a, '/content/modules') " +
                "or isdescendantnode(a, '/content/sandboxes')) " + query +
                querySuffix, Query.JCR_SQL2);

        List<Map<String, Object>> ret = new ArrayList<>();

        while (resources.hasNext()) {
            Resource r = resources.next();
            Map<String, Object> m = new HashMap(r.getValueMap());
            ret.add(m);
            log.trace(r.getName());
            m.put("name", r.getName());
            m.put("pant:transientPath", r.getPath().substring(PATH_SUBSTRING_INDEX));
            // Example path: /content/repositories/ben_2019-04-11_16-15-15/shared/attributes.module.adoc
            String[] fragments = r.getPath().split("/");
            // Example fragments: ["", "content", "repositories", "ben_2019-04-11_16-15-15", "shared", "attributes.module.adoc"]
            m.put("pant:transientSource", fragments[2]);
            if (!"modules".equals(fragments[2])) {
                m.put("pant:transientSourceName", fragments[3]);
            }
            for (Map.Entry<String, Object> e : m.entrySet()) {
                log.trace("{} :: {} :: {}", e.getKey(), e.getValue().getClass(), e.getValue());
            }
            log.trace("");
        }

        return ret;
    }
}
