package com.redhat.pantheon.helper;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Arrays;

public class Symlinks {

    public static Resource resolve(ResourceResolver resolver, String path) {
        Resource res = resolver.resolve(path);

        if (res != null && !"sling:nonexisting".equals(res.getResourceType())) {
            return res;
        } else {
            //Because of symlinks, we need to resolve path manually.
            String[] parts = path.split("/");
            StringBuilder realPath = new StringBuilder();
            for (String part : parts) {
                if (part.isEmpty()) {
                    continue;
                }
                realPath.append("/").append(part);
                Resource resource = resolver.resolve(realPath.toString());
                if ("pantheon/symlink".equals(resource.getResourceType())) {
                    realPath.setLength(realPath.length() - part.length());
                    realPath.append(resource.getValueMap().get("pant:target", String.class));
                }
            }

            res = resolver.resolve(realPath.toString().replaceAll("/+", "/"));
            if (Arrays.stream(res.getPath().split("/")).anyMatch(t -> ".".equals(t) || "..".equals(t))
                    && !res.getPath().equals(realPath.toString())) {
                // Compensating for glitchy sling code here.
                // If you ask sling for something like this:
                // /content/docs/Pantheon/assemblies/../modules/ref_pantheon-user-interface.adoc
                // ...and that resource doesn't exist, sling gives you this instead:
                // /content/docs/Pantheon/assemblies/.
                // ...which is working reference to the sling:Folder rather than a sling:nonexisting to the file like
                // one might expect. I'm not sure why and this is my best effort to detect that scenario.
                res = resolver.resolve("/dummyNonExisting");
            }
            return res;
        }
    }
}
