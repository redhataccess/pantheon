package com.redhat.pantheon.upgrade;

import com.google.common.collect.Lists;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import java.util.List;

@Component(service = UpgradeProvider.class)
public class DefaultUpgradeProvider implements UpgradeProvider {
    @Override
    public List<Upgrade> getUpgrades() {
        return Lists.newArrayList(
                new Upgrade() {
                    @Override
                    public void run(ResourceResolver resourceResolver, final Appendable log)
                            throws Exception {
                        log.append("#### This is the first upgrade running ####");
                        log.append("#### And this is the first upgrade finishing ####");
                    }

                    @Override
                    public String getId() {
                        return "sample-upgrade";
                    }
                }
        );
    }
}
