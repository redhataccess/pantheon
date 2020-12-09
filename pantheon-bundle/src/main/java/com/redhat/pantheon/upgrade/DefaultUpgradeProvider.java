package com.redhat.pantheon.upgrade;

import com.google.common.collect.Lists;
import com.redhat.pantheon.upgrade.impl.CopyRHELContent;
import org.osgi.service.component.annotations.Component;

import java.util.List;

/**
 * Default implementation of an {@link UpgradeProvider}. Simply builds
 * and return a static list of upgrades.
 */
@Component(service = UpgradeProvider.class)
public class DefaultUpgradeProvider implements UpgradeProvider {

    private static final List<Upgrade> UPGRADES = Lists.newArrayList(
            new CopyRHELContent()
    );

    @Override
    public List<Upgrade> getUpgrades() {
        return UPGRADES;
    }
}
