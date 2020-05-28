package com.redhat.pantheon.sling.upgrade;

import org.apache.sling.api.resource.ResourceResolver;

/**
 * Implements a single, atomic repository upgrade.
 * @author Carlos Munoz
 */
public interface RepositoryUpgrade {

    /**
     * @return A unique identifier for this upgrade.
     */
    String getId();

    /**
     * @return A free-form textual description of what the upgrade is about.
     */
    default String getDescription() {
        return "";
    }

    /**
     * This is the logic executed as part of the upgrade.
     * Implementors should take care not to use the {@link ResourceResolver#commit()} or
     * {@link ResourceResolver#close()} methods as part of the upgrade as this will break the
     * upgrade process.
     * @param resourceResolver The {@link ResourceResolver} to modify the JCR repository.
     * @throws Exception If ANY problem is encountered. Any exception will rollback the changes
     *  done by the upgrade, and will stop the upgrade process.
     */
    void executeUpgrade(ResourceResolver resourceResolver) throws Exception;
}
