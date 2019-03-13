package com.redhat.pantheon.sling;

import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.api.SlingRepositoryInitializer;

/**
 * Created by ben on 3/7/19.
 */
public class PantheonInitializer implements SlingRepositoryInitializer {

    @Override
    public void processRepository(SlingRepository slingRepository) throws Exception {
        //May be a good place to autocreate users at some point in the future
    }
}
