package com.redhat.pantheon.upgrade;

import java.util.List;

/**
 * Component interface which provides the ordered list of upgrades to run.
 */
public interface UpgradeProvider {

    /**
     * @return The ordered list of upgrades available for the system.
     */
    List<Upgrade> getUpgrades();
}
