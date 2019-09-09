package com.redhat.pantheon.extension;

import com.redhat.pantheon.extension.events.ModuleRevisionPublishedEvent;

/**
 * Interface that processes {@link ModuleRevisionPublishedEvent} events. OSGI-declared components (i.e.
 * having the @{@link org.osgi.service.component.annotations.Component} annotation) which
 * implement this interface will be picked up by the matching {@link EventJobConsumer} and will
 * be invoked when such an event is fired.
 *
 * @author Carlos Munoz
 */
public interface ModulePostPublishExtension extends EventProcessingExtension<ModuleRevisionPublishedEvent> {
}
