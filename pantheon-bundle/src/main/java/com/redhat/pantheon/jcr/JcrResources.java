package com.redhat.pantheon.jcr;


import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;

public class JcrResources {
    private JcrResources() {
    }

    public static void rename(Resource resource, String newName) throws RepositoryException {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        Session session = resourceResolver.adaptTo(Session.class);
        session.move(resource.getPath(), resource.getParent().getPath() + "/" + newName);
    }

    /**
     * calculates a checksum hash for a string using the adler32 algorithm
     *
     */
    public static HashCode hash(String str) {
        return Hashing.adler32().hashString(str == null ? "" : str, Charsets.UTF_8);
    }

    /**
     * Calculates a checksum hash for the contents for an input stream usng the adler32 algorithm.
     * @param is The input stream. The stream is NOT closed after reading from it.
     * @return A {@link HashCode} for the contents of the given InputStream
     * @throws IOException If there is a problem accessing the contents of the InputStream
     */
    public static HashCode hash(InputStream is) throws IOException {
        try (HashingInputStream his = new HashingInputStream(Hashing.adler32(), is)) {
            while (his.read() != -1) ;
            return his.hash();
        }
    }
}
