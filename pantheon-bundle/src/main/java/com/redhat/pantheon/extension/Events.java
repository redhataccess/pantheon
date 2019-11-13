package com.redhat.pantheon.extension;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

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

    public void fireEvent(Event evt) {
        Map<String, Object> props = newHashMap();
        props.put(Event.class.getName(), evt);
        Job job = jobManager.createJob(EVENT_TOPIC_NAME)
                .properties(props)
                .add();

        if(job == null) {
            throw new RuntimeException("Something went wrong firing a " + evt.getClass().getName() + " event");
        }
    }
}
