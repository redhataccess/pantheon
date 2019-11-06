package com.redhat.pantheon.extension;

/**
 * Common interface for extensions to be triggered after a module publication event occurs in the
 * system.
 *
 * @author Carlos Munoz
 */
public interface EventProcessingExtension<E extends Event> {

    /**
     * Processes an event, or throws an Exception if a problem is encountered.
     * @throws Exception if there is a problem with event processing
     */
    void processEvent(E event) throws Exception;
}
