package com.redhat.pantheon.sling.upgrade;

import com.google.common.collect.Maps;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import com.redhat.pantheon.sling.upgrade.impl.IntroduceModuleVariantRepositories;
import com.redhat.pantheon.sling.upgrade.impl.RefactorModulesForVariants;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.api.SlingRepositoryInitializer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Upgrades the Sling repository based on the upgrade components present.
 * Upgrades are processed in order and independently as a single atomic operation.
 * If an upgrade succeeds, it's changes are committed and a record is kept in JCR
 * detailing the change. Conversely if an upgrade fails, all changes are rolled back and
 * an exception is thrown. Hence upgrades will be idempotent and running the upgrade
 * process multiple times should not have any side-effects.
 *
 * @author Carlos Munoz
 */
@Component(
        service = SlingRepositoryInitializer.class
)
public class PantheonUpgrader implements SlingRepositoryInitializer {

    private static final Logger log = LoggerFactory.getLogger(PantheonUpgrader.class);
    private static final String PANTHEON_UPGRADE_BASE = "/conf/pantheon/upgrades";
    private final ServiceResourceResolverProvider serviceResourceResolverProvider;

    @Activate
    public PantheonUpgrader(@Reference ServiceResourceResolverProvider serviceResourceResolverProvider) {
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
    }

    /**
     * The list of upgrades in the order they want to ge executed. Ideally only additions to the
     * bottom of the list should be made. Other additions or removals will have undetermined effects.
     */
    // TODO hard-code the list of upgrades for now. It could be a @Reference in the future
    private List<RepositoryUpgrade> getUpgrades() {
        return newArrayList(
                // Manually curated list of upgrades ordered for execution
                new IntroduceModuleVariantRepositories(),
                new RefactorModulesForVariants()
        );
    }

    @Override
    public void processRepository(SlingRepository repo) throws Exception {
        for (RepositoryUpgrade upgrade : getUpgrades()) {
            try (ResourceResolver resourceResolver = serviceResourceResolverProvider.getServiceResourceResolver()) {
                Resource upgradeResource = resourceResolver.getResource(PANTHEON_UPGRADE_BASE + "/" + upgrade.getId());

                if (upgradeResource == null) {
                    log.info("Running upgrade: " + upgrade.getId());
                    upgrade.executeUpgrade(resourceResolver);
                    recordSuccessfulUpgrade(resourceResolver, upgrade);
                    resourceResolver.commit();
                }
                else {
                    log.debug("Skipping already executed upgrade: " + upgrade.getId());
                }
            } catch (Exception ex) {
                log.error("Error running repository upgrade: " + upgrade.getClass().getName(), ex);
                break;
            }
        }
    }

    private void recordSuccessfulUpgrade(ResourceResolver resourceResolver, RepositoryUpgrade upgrade) throws PersistenceException {
        Resource upgradesFolder =
                ResourceUtil.getOrCreateResource(resourceResolver, PANTHEON_UPGRADE_BASE, "sling:OrderedFolder", "sling:Folder", false);
        Map<String, Object> props = Maps.newHashMap();
        props.put(JcrConstants.JCR_PRIMARYTYPE, "nt:unstructured");
        Resource upgradeResource = resourceResolver.create(upgradesFolder, upgrade.getId(), props);
        ModifiableValueMap upgradeResProps = upgradeResource.adaptTo(ModifiableValueMap.class);
        upgradeResProps.put("description", upgrade.getDescription());
        upgradeResProps.put("implClass", upgrade.getClass().getName());
        upgradeResProps.put(JcrConstants.JCR_CREATED, Calendar.getInstance());
    }
}
