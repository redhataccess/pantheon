package com.redhat.pantheon.use;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.scripting.sightly.pojo.Use;

import javax.jcr.query.Query;
import javax.script.Bindings;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ben on 3/5/19.
 */
public class ModuleData implements Use {

    private ResourceResolver resolver;
    private Resource currentResource;

    @Override
    public void init(Bindings bindings) {
        resolver = (ResourceResolver) bindings.get("resolver");
        currentResource = (Resource) bindings.get("resource");
    }

    public List<Resource> getModulesNameSort() {
        return getModules("order by a.[jcr:title] asc");
    }

    public List<Resource> getModulesCreateSort() {
        return getModules("order by a.[jcr:created] desc");
    }

    private List<Resource> getModules(String querySuffix) {
        Iterator<Resource> resources = resolver.findResources("select * from [sling:Folder] as a " +
                "where [sling:resourceType] = 'pantheon/modules' " +
                "and isdescendantnode(a, '/content/modules') " +
                querySuffix, Query.JCR_SQL2);
        List<Resource> ret = new ArrayList<>();
        resources.forEachRemaining(ret::add);
        return ret;
    }
}
