package com.redhat.pantheon.scripts.impl;

import com.redhat.pantheon.scripts.Script;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.pipes.Plumber;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

//@Component(service = SampleScript.class)
public class SampleScript implements Script {

    private Plumber plumber;

    public SampleScript(@Reference Plumber plumber) {
        this.plumber = plumber;
    }

    @Override
    public void run(ResourceResolver resourceResolver) throws Exception {
        plumber.newPipe(resourceResolver)
                .$("nt:base")
                //.xpath("/jcr:root/content/repositories//element(*,pant:module)")
                .traverse()
                .run();
    }
}
