/**
 * Admin script to run all upgrades on the system.
 * This script will only run previously non-executed scripts
 */
def upgradeRunner = getService(com.redhat.pantheon.upgrade.UpgradeRunner)
upgradeRunner.runUpgrades()