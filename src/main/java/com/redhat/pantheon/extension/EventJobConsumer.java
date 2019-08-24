package com.redhat.pantheon.extension;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

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
 * @param <EXT>
 *
 * @author Carlos Munoz
 */
abstract class EventJobConsumer<EXT extends EventProcessingExtension> implements JobConsumer {

    public static final Logger log = LoggerFactory.getLogger(EventJobConsumer.class);

    /** The specific extension class this job consumer satisfies */
    protected final Class<EXT> extensionClass;

    protected EventJobConsumer(Class<EXT> extensionClass) {
        this.extensionClass = extensionClass;
    }

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
            log.error(this.getClass().getName() + " fired an even job with no event");
            return JobResult.CANCEL;
        }

        try {
            getExtensions().forEach(service -> {
                try {
                    service.processEvent(firedEvent);
                    log.trace("Extension " + service.getClass().getName() + " finished successfully");
                } catch (Throwable t) {
                    log.warn("Extension " + service.getClass().getName() + " did not execute successfully", t);
                }
            });
        } catch (InvalidSyntaxException e) {
            // This should not happen since we are not filtering the services (the filter parameter is null)
            log.error("Invalid filter syntax", e);
            return JobResult.CANCEL;
        }
        return JobResult.OK;
    }

    /**
     * Collects all the services registered as implementations of the given interface.
     *
     * @return A set of extension services which implement the extension interface for this consumer
     */
    protected Collection<EXT> getExtensions() throws InvalidSyntaxException {
        List<EXT> extensions = newArrayList();
        BundleContext bundleContext = FrameworkUtil.getBundle(extensionClass).getBundleContext();
        Collection<ServiceReference<EXT>> serviceReferences =
                bundleContext
                        .getServiceReferences(extensionClass, null);

        for (ServiceReference<EXT> reference : serviceReferences) {
            EXT service = bundleContext.getService(reference);
            extensions.add(service);
        }
        return extensions;
    }
}
