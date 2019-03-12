package com.redhat.pantheon.sling;

import com.redhat.pantheon.use.PlatformData;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Created by ben on 3/7/19.
 */
public class PantheonBundle implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        System.out.println("Pantheon Bundle activated - built:" + PlatformData.getJarBuildDate());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        //FIXME
//        if (asciidoctor != null) {
//            asciidoctor.shutdown();
//        }
    }
}
