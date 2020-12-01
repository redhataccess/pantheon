package com.redhat.pantheon.scripts;

import org.apache.sling.api.resource.ResourceResolver;

public interface Script {

    default String getId() {
        return this.getClass().getName();
    }

    void run(ResourceResolver resourceResolver) throws Exception;
}
