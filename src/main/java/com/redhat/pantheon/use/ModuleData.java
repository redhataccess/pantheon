package com.redhat.pantheon.use;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.Query;
import javax.script.Bindings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ben on 3/5/19.
 */
public class ModuleData implements Use {

    private static final int PATH_SUBSTRING_INDEX = "/content/".length();

    private ResourceResolver resolver;
    private Resource currentResource;
    private String searchTerm;

    private final Logger log = LoggerFactory.getLogger(ModuleData.class);

    @Override
    public void init(Bindings bindings) {
        resolver = (ResourceResolver) bindings.get("resolver");
        currentResource = (Resource) bindings.get("resource");
        searchTerm = (String) bindings.get("query");
    }

    public List<Map<String, Object>> getModulesNameSort() {
        return getModules(searchTerm, "order by a.[jcr:title] asc");
    }

    public List<Map<String, Object>> getModulesCreateSort() {
        return getModules(searchTerm, "order by a.[jcr:created] desc");
    }

    private List<Map<String, Object>> getModules(String query, String querySuffix) {
      	if (query.equals("") || query.equals("*") || query == null) {
      	    query = "";
      	} else {
      		query = "AND (a.[jcr:title] like "+ "'%"+query+"%' "+
      				"OR a.[jcr:description] like "+ "'%"+query+"%') ";
      	}

        Iterator<Resource> resources = resolver.findResources("select * from [pant:module] as a " +
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
