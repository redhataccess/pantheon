package com.redhat.pantheon.extension;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static com.redhat.pantheon.extension.Events.EVENT_TOPIC_NAME;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

/**
 * Base class for all Job consumers for fired events. It contains most of the logic necessary to
 * implement an event job consumer. Since these job consumers are bound to specific extension interfaces,
 * each concrete implementation will only process one type of extension. Caution must be taken so
 * that specific extension types are not processed by multiple concrete EventJobConsumers, as the results
 * would be undefined.
 *
 * Subclasses of this are necessary only to declare the right OSGI component configuration so that each
 * type of event can be sent to a different queue. If this is not desired, this class should be sufficient to
 * process ALL events from a single queue.
 *
 * @author Carlos Munoz
 */
@Component(
        service = JobConsumer.class,
        property = JobConsumer.PROPERTY_TOPICS + "=" + EVENT_TOPIC_NAME
)
public class EventJobConsumer implements JobConsumer {

    public static final Logger log = LoggerFactory.getLogger(EventJobConsumer.class);

    @Reference(policy=DYNAMIC)
    final List<EventProcessingExtension> extensions = new CopyOnWriteArrayList<>();

    /**
     * Generic method that processes a job. The job will always check for the presence of
     * an event in the job properties, and an error will be reported if no such property
     * exists.
     * @param job The event job to process
     * @return OK in almost all circumstances, even if some extensions execute incorrectly.
     */
    @Override
    public final JobResult process(final Job job) {
        final Event firedEvent = job.getProperty(Event.class.getName(), Event.class);

        if (firedEvent == null) {
            log.error(this.getClass().getName() + " fired an event job with no event");
            return JobResult.CANCEL;
        }

        extensions.stream().forEach(ext -> {
            try {
                if(ext.canProcessEvent(firedEvent)) {
                    ext.processEvent(firedEvent);
                    log.trace("Extension " + ext.getClass().getName() + " finished successfully");
                }
            } catch (Throwable t) {
                log.warn("Extension " + ext.getClass().getName() + " did not execute successfully", t);
            }
        });
        return JobResult.OK;
    }
}
