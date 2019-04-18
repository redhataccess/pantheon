package com.redhat.pantheon.use;

import com.redhat.pantheon.data.ModuleDataRetriever;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import java.util.List;
import java.util.Map;

/**
 * Created by ben on 3/5/19.
 */
public class ModuleData implements Use {

    private String searchTerm;
    private ModuleDataRetriever retriever;

    private final Logger log = LoggerFactory.getLogger(ModuleData.class);

    @Override
    public void init(Bindings bindings) {
        searchTerm = (String) bindings.get("query");
        retriever = new ModuleDataRetriever((ResourceResolver) bindings.get("resolver"));
    }

    public List<Map<String, Object>> getModulesNameSort() {
        return retriever.getModulesNameSort(searchTerm);
    }

    public List<Map<String, Object>> getModulesCreateSort() {
        return retriever.getModulesCreateSort(searchTerm);
    }
}
