package com.redhat.pantheon.upgrade;

import org.apache.sling.api.resource.ResourceResolver;

/**
 * Represents the behaviour that a single system upgrade implements.
 * System upgrades may generally change the JCR tree, or perform other
 * back-end configuration changes necessary from one system version to
 * the next.
 */
@FunctionalInterface
public interface Upgrade {
    /**
     * The upgrade identifier. This is a unique identifier for all upgrades in the
     * system.
     * @return A unique identifier for a particular upgrade.
     */
    default String getId() {
        return this.getClass().getName();
    }

    /**
     * Performs the main upgrade logic.
     * @param resourceResolver A {@link ResourceResolver} to use for performing data
     *                         changes.
     * @param log An appendable to store all loggable messages while performing the upgrade.
     * @throws Exception In the scenario where there is an unrecoverable error while performing
     * the upgrade. Throwing an exception means that the upgrade process should be immediately
     * stopped. It does not however guarantee that all commited changes will be rolled back.
     * Implementing classes should take care when commiting changes in the resource resolver
     * in the middle of an upgrade.
     */
    void run(ResourceResolver resourceResolver, Appendable log) throws Exception;

    /**
     * Performs a reversal of this upgrade.
     * <br><br>
     * Note: This method is here for a future implementation. The current upgrade system
     * mechanism does not perform roll backs.
     * @param resourceResolver A {@link ResourceResolver} to use for performing data
     *                         changes.
     * @param log An appendable to store all loggable messages while performing the roll back.
     * @throws Exception If there is an error rolling back the upgrade.
     */
    default void rollback(ResourceResolver resourceResolver, Appendable log) throws Exception {
        throw new UnsupportedOperationException("Rollback operation is not implemented for this upgrade");
    }
}
