package com.redhat.pantheon.upgrade;

import com.google.common.collect.Lists;
import com.redhat.pantheon.upgrade.impl.CopyRHELContent;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import java.util.List;

@Component(service = UpgradeProvider.class)
public class DefaultUpgradeProvider implements UpgradeProvider {
    @Override
    public List<Upgrade> getUpgrades() {
        return Lists.newArrayList(
                new CopyRHELContent()
        );
    }
}
