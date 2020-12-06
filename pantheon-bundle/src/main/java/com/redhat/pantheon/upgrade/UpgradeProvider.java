package com.redhat.pantheon.upgrade;

import java.util.List;

public interface UpgradeProvider {
    List<Upgrade> getUpgrades();
}
