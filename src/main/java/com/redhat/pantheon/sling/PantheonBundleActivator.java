package com.redhat.pantheon.sling;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Created by ben on 3/7/19.
 */
public class PantheonBundleActivator implements BundleActivator {

    private static BundleContext ctx;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        System.out.println("Pantheon Bundle Activated");
        ctx = bundleContext;
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }

    public static BundleContext getContext() {
        return ctx;
    }
}
