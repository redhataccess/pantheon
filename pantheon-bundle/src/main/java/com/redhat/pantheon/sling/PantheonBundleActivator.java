package com.redhat.pantheon.sling;

import com.redhat.pantheon.use.PlatformData;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Created by ben on 3/7/19.
 */
public class PantheonBundleActivator implements BundleActivator {

    private Object registration;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
//        Hashtable props = new Hashtable();
//        props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, "/*");
////        props.put(KeycloakOIDCFilter.CONFIG_RESOLVER_PARAM, new MyConfigResolver());
//
//        this.registration = bundleContext.registerService(Filter.class.getName(), new KeycloakOIDCFilter(), props);
        System.out.println("Pantheon Bundle activated - built:" + PlatformData.getJarBuildDate());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
//        this.registration.unregister();
    }
}
