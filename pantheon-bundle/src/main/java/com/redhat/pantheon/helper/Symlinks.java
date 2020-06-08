package com.redhat.pantheon.helper;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

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

            return resolver.resolve(realPath.toString().replaceAll("/+", "/"));
        }
    }
}
