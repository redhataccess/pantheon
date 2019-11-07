package com.redhat.pantheon.extension;

import com.redhat.pantheon.extension.events.ModuleVersionPublishedEvent;
import com.redhat.pantheon.model.module.ModuleVersion;
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
    public static final String MODULE_POST_PUBLISH_EVENT = "com/redhat/pantheon/ModulePostPublish";

    private JobManager jobManager;

    @Activate
    public Events(@Reference JobManager jobManager) {
        this.jobManager = jobManager;
    }

    /**
     * Fires an event indicating that a module version has been publsihed. As its name implies,
     * this event should be fired only after a module is published.
     * @param moduleVersion The module version which has just been published.
     */
    public void fireModuleVersionPublishedEvent(ModuleVersion moduleVersion) {
        ModuleVersionPublishedEvent event = new ModuleVersionPublishedEvent(moduleVersion.getPath());
        Map<String, Object> props = newHashMap();
        props.put(Event.class.getName(), event);
        
        
        Job job = jobManager.createJob(MODULE_POST_PUBLISH_EVENT)
                .properties(props)
                .add();

        if(job == null) {
            throw new RuntimeException("Something went wrong creating a " + MODULE_POST_PUBLISH_EVENT + " job");
        }
        
//      Module module = moduleVersion.getParent().getParent().adaptTo(Module.class);
//      ModuleVersionPublishedEvent event2 = new ModuleVersionPublishedEvent(module.get().getValueMap().containsKey("jcr:uuid"));
    }
}
