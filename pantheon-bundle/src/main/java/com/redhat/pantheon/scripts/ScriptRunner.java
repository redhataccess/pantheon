package com.redhat.pantheon.scripts;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.redhat.pantheon.scripts.impl.SampleScript;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.api.SlingRepositoryInitializer;
import org.apache.sling.pipes.Plumber;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Component(service = SlingRepositoryInitializer.class)
public class ScriptRunner implements SlingRepositoryInitializer {

    private static final Logger log = LoggerFactory.getLogger(ScriptRunner.class);
    private static final String PANTHEON_SCRIPT_BASE = "/conf/pantheon/scripts";
    private ServiceResourceResolverProvider serviceResourceResolverProvider;
    private BundleContext bundleContext;
    private Plumber plumber;

    @Activate
    public ScriptRunner(BundleContext context,
                        @Reference ServiceResourceResolverProvider serviceResourceResolverProvider/*,
                        @Reference Plumber plumber*/) {
        this.bundleContext = context;
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
//        this.plumber = plumber;
    }

    @Override
    public void processRepository(SlingRepository repo) throws Exception {
        try (ResourceResolver resourceResolver = serviceResourceResolverProvider.getServiceResourceResolver()) {
            for (Script script : getAllScripts()) {
                Resource upgradeResource = resourceResolver.getResource(PANTHEON_SCRIPT_BASE + "/" + script.getId());

                if (upgradeResource == null) {
                    log.info("Running upgrade: " + script.getId());
                    script.run(resourceResolver);
                    recordSuccessfulScriptRun(resourceResolver, script);
                    resourceResolver.commit();
                } else {
                    log.debug("Skipping already executed upgrade: " + script.getId());
                }
            }
        } catch (Exception ex) {
            log.error("Error running repository scripts", ex);
        }
    }

    private List<Script> getAllScripts() throws InvalidSyntaxException {
        // TODO There might be a better way to do this dynamically
        //  OSGI is VERY dynamic, and there is no guarantee that the Scripts will be registered when this runs
        return Lists.newArrayList(
                new SampleScript(plumber)
        );
    }

    private void recordSuccessfulScriptRun(ResourceResolver resourceResolver, Script script) throws PersistenceException {
        Resource upgradesFolder =
                ResourceUtil.getOrCreateResource(resourceResolver, PANTHEON_SCRIPT_BASE, "sling:OrderedFolder", "sling:Folder", false);
        Map<String, Object> props = Maps.newHashMap();
        props.put(JcrConstants.JCR_PRIMARYTYPE, "nt:unstructured");
        Resource upgradeResource = resourceResolver.create(upgradesFolder, script.getId(), props);
        ModifiableValueMap upgradeResProps = upgradeResource.adaptTo(ModifiableValueMap.class);
        //upgradeResProps.put("description", script.getDescription());
        upgradeResProps.put("implClass", script.getClass().getName());
        upgradeResProps.put(JcrConstants.JCR_CREATED, Calendar.getInstance());
    }
}
