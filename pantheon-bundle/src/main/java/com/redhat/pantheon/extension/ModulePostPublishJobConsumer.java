package com.redhat.pantheon.extension;

import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;

import static com.redhat.pantheon.extension.Events.MODULE_POST_PUBLISH_EVENT;

/**
 * A consumer for Module post publish events.
 * This consumer makes sure that the events are processed, and any possible errors
 * are reported. It will call on all registered OSGI components under the {@link EventProcessingExtension}
 * service interface and will report if there is a failure. If any extension fails its processing,
 * no retries will be attempted. It is up to the extension developer to make sure their extensions are
 * working properly.
 */
@Component(
        service = JobConsumer.class,
        property = JobConsumer.PROPERTY_TOPICS + "=" + MODULE_POST_PUBLISH_EVENT
)
public class ModulePostPublishJobConsumer extends EventJobConsumer<ModulePostPublishExtension> {

    public ModulePostPublishJobConsumer() {
        super(ModulePostPublishExtension.class);
    }
}
