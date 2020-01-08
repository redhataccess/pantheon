package com.redhat.pantheon.extension;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Date;
import java.util.Map;

import org.apache.sling.event.jobs.JobBuilder.ScheduleBuilder;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with the publication of application events, which are the basis for extensions.
 * Application events are internally enabled using Sling jobs, and this class makes sure
 * there are type-safe ways of accessing these extension points.
 * @author Carlos Munoz
 */
@Component(service = Events.class)
public class Events {

    private static final Logger log = LoggerFactory.getLogger(Events.class);
    public static final String EVENT_TOPIC_NAME = "com/redhat/pantheon/Event";

    private JobManager jobManager;

    @Activate
    public Events(@Reference JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public void fireEvent(Event evt, int delayInsecs) {
        final long delay = delayInsecs * 1000;
        final Date fireDate = new Date();
        fireDate.setTime(System.currentTimeMillis() + delay);
        Map<String, Object> props = newHashMap();
        props.put(Event.class.getName(), evt);
        ScheduleBuilder scheduleBuilder = jobManager.createJob(EVENT_TOPIC_NAME)
                .properties(props)
                .schedule();
        scheduleBuilder.at(fireDate);

        if(scheduleBuilder.add() == null) {
            throw new RuntimeException("Something went wrong scheduling a " + evt.getClass().getName() + " event");
        }
    }
}
