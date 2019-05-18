package com.redhat.pantheon.data;

import com.redhat.pantheon.jcr.JcrQueryHelper;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isEmpty;

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

	public List<Map<String, Object>> getModulesSort(String searchTerm, String key, String direction) {
		if (searchTerm == null || searchTerm.isEmpty()) {
			searchTerm.equals("*");
		}
		if (key == null || (!key.equals("jcr:title") && !key.equals("jcr:description"))) {
			key = "jcr:created";
		}
		if (direction == null || !direction.equals("desc")) {
			direction = "asc";
		}
        try {
            return getModules(searchTerm, "a.[" + key + "] " + direction);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, Object>> getModulesNameSort(String searchTerm) {
        try {
            return getModules(searchTerm, "a.[jcr:title] asc");
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, Object>> getModulesCreateSort(String searchTerm) {
        try {
            return getModules(searchTerm, "a.[jcr:created] desc");
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, Object>> getModules(String query, String orderBy)
            throws RepositoryException {
        if (query.equals("") || query.equals("*") || query == null) {
            query = "";
        } else {
            query = "AND (a.[jcr:title] like " + "'%" + query + "%' " +
                    "OR a.[jcr:description] like " + "'%" + query + "%') ";
        }
        
        log.trace("Ordering query: " + orderBy );

        //FIXME - we had "select * from [pant:module]..." here, BUT we were seeing problems that after a very small
        //FIXME - number of module upload/delete operations, this query would suddenly return only a very small number
        //FIXME - of modules. Changing this to [nt:base] seems to fix it, but I don't know why. Perhaps it's some bug
        //FIXME - related to 'nodetypes.cnd' getting reinstalled on every package deployment, resulting in the
        //FIXME - pant:module nodetype being assigned some new internal id, but that's pure speculation.
        StringBuilder queryBuilder = new StringBuilder()
                .append("select * from [nt:base] as a ")
                .append("where [sling:resourceType] = 'pantheon/modules' ")
                .append("and (isdescendantnode(a, '/content/repositories') ")
                .append("or isdescendantnode(a, '/content/modules') ")
                .append("or isdescendantnode(a, '/content/sandboxes')) ")
                .append(query);
        if (!isEmpty(orderBy)) {
            queryBuilder.append(" order by ").append(orderBy);
        }

        Stream<Resource> results = new JcrQueryHelper(resolver).query(queryBuilder.toString());

        return results.map(r -> {
            Map<String, Object> m = new HashMap(r.getValueMap());
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
            return m;
        })
        .collect(Collectors.toList());
    }
}
