package com.redhat.pantheon.dependency;

import com.redhat.pantheon.sling.PantheonBundle;
import org.osgi.framework.FrameworkUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by ben on 3/12/19.
 */
public class OsgiDependencyProvider extends DependencyProvider {

    private static File templateDir;

    @Override
    public List<String> getGemPaths() {
        Enumeration<URL> gems = FrameworkUtil.getBundle(OsgiDependencyProvider.class).findEntries("gems", "*", false);
        List<String> gemPaths = new ArrayList<>();
        while (gems.hasMoreElements()) {
            URL g = gems.nextElement();
            gemPaths.add("uri:classloader:" + g.getPath() + "lib");
        }
        return gemPaths;
    }

    @Override
    public File getTemplateDir() throws IOException {
        if (templateDir == null) {
            Enumeration<URL> urls = FrameworkUtil.getBundle(PantheonBundle.class).findEntries("apps/pantheon/templates/haml/html5", "*", false);
            Path p = Files.createTempDirectory("templates");
            templateDir = p.toFile();

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String filename = url.toString();
                filename = filename.substring(filename.lastIndexOf("/") + 1);

                File f = new File(templateDir, filename);
                f.deleteOnExit();

                InputStream is = url.openConnection().getInputStream();
                FileOutputStream os = new FileOutputStream(f);

                byte[] buffer = new byte[4096];

                int n;
                while (-1 != (n = is.read(buffer))) {
                    os.write(buffer, 0, n);
                }

                os.flush();
                os.close();
                is.close();
            }
        }
        return templateDir;
    }
}
