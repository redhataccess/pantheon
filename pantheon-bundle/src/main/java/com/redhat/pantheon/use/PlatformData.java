package com.redhat.pantheon.use;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import java.util.Date;
import java.util.jar.Manifest;

/**
 * Created by ben on 3/7/19.
 */
public class PlatformData implements Use {

    private ResourceResolver resolver;
    private Resource currentResource;

    private static final Logger log = LoggerFactory.getLogger(PlatformData.class);

    @Override
    public void init(Bindings bindings) {
        resolver = (ResourceResolver) bindings.get("resolver");
        currentResource = (Resource) bindings.get("resource");
    }

    public static String getJarBuildDate() {
        String ret = "Unable to determine build date.";
        try {
            Manifest mf = new Manifest(PlatformData.class.getClassLoader().getResource("META-INF/MANIFEST.MF").openStream());
            String lastmod = mf.getMainAttributes().getValue("Bnd-LastModified");
            Date d = new Date(Long.valueOf(lastmod));
            ret = d.toString();
            log.info(ret);
        } catch (Exception e) {
            log.error(ret, e);
        }
        return ret;
    }
}
