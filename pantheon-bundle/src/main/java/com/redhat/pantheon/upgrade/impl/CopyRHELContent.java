package com.redhat.pantheon.upgrade.impl;

import com.redhat.pantheon.upgrade.Upgrade;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Session;
import java.util.Collections;

public class CopyRHELContent implements Upgrade {

    @Override
    public String getId() {
        return "copy-rhel-content";
    }

    @Override
    public void run(ResourceResolver resourceResolver, Appendable log) throws Exception {
        resourceResolver.create(resourceResolver.getResource("/content/repositories"), "backup", Collections.emptyMap());
        resourceResolver.copy("/content/repositories/rhel-8-docs", "/content/repositories/backup");
        resourceResolver.adaptTo(Session.class).move("/content/repositories/backup/rhel-8-docs",
                "/content/repositories/rhel-8-docs-copy");
        resourceResolver.delete(resourceResolver.getResource("/content/repositories/backup"));
    }
}
