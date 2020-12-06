package com.redhat.pantheon.upgrade;

import com.google.common.collect.Maps;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Map;

@Component(service = UpgradeRunner.class)
public class UpgradeRunner {
    private static final Logger log = LoggerFactory.getLogger(UpgradeRunner.class);
    private static final String UPGRADE_BASE = "/conf/pantheon/upgrades";
    private ServiceResourceResolverProvider serviceResourceResolverProvider;
    private UpgradeProvider upgradeProvider;

    @Activate
    public UpgradeRunner(@Reference ServiceResourceResolverProvider serviceResourceResolverProvider,
                         @Reference UpgradeProvider upgradeProvider) {
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
        this.upgradeProvider = upgradeProvider;
    }

    public void runUpgrades() {
        try (ResourceResolver resourceResolver = serviceResourceResolverProvider.getServiceResourceResolver()) {
            for (Upgrade upgrade : upgradeProvider.getUpgrades()) {
                Resource upgradeResource = resourceResolver.getResource(UPGRADE_BASE + "/" + upgrade.getId());

                if (upgradeResource == null) {
                    log.info("Running upgrade: " + upgrade.getId());
                    StringBuilder logCapture = new StringBuilder();
                    upgrade.run(resourceResolver, new AppendableDecorator(logCapture) {
                        @Override
                        public CharSequence decorate(CharSequence csq) {
                            return csq + System.lineSeparator();
                        }
                    });
                    recordSuccessfulUpgrade(resourceResolver, upgrade, logCapture.toString());
                    resourceResolver.commit();
                } else {
                    log.debug("Skipping already executed upgrade: " + upgrade.getId());
                }
            }
        } catch (Exception ex) {
            log.error("Error running repository scripts", ex);
        }
    }

    private void recordSuccessfulUpgrade(ResourceResolver resourceResolver, Upgrade upgrade,
                                         @Nullable String log)
            throws PersistenceException {
        Resource upgradesFolder =
                ResourceUtil.getOrCreateResource(resourceResolver, UPGRADE_BASE, "sling:OrderedFolder", "sling:Folder", false);
        Map<String, Object> props = Maps.newHashMap();
        props.put(JcrConstants.JCR_PRIMARYTYPE, "nt:unstructured");
        Resource upgradeResource = resourceResolver.create(upgradesFolder, upgrade.getId(), props);
        ModifiableValueMap upgradeResProps = upgradeResource.adaptTo(ModifiableValueMap.class);
        upgradeResProps.put("implClass", upgrade.getClass().getName());
        upgradeResProps.put(JcrConstants.JCR_CREATED, Calendar.getInstance());
        upgradeResProps.put("log", log);
    }
}
