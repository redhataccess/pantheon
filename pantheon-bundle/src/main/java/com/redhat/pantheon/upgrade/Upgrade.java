package com.redhat.pantheon.upgrade;

import org.apache.sling.api.resource.ResourceResolver;

@FunctionalInterface
public interface Upgrade {
    default String getId() {
        return this.getClass().getName();
    }

    void run(ResourceResolver resourceResolver, Appendable log) throws Exception;

    default void rollback(ResourceResolver resourceResolver, Appendable log) throws Exception {
        throw new UnsupportedOperationException("Rollback operation is not implemented for this upgrade");
    }
}
