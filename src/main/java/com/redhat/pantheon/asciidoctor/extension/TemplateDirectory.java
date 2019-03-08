package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.sling.PantheonBundleActivator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

/**
 * Created by ben on 3/8/19.
 */
public class TemplateDirectory {

    private static File tmplDir;

    public static File get() {
        if (tmplDir == null) {
            Enumeration<URL> urls = PantheonBundleActivator.getContext().getBundle().findEntries("apps/pantheon/templates/haml/html5", "*", false);
            try {
                Path p = Files.createTempDirectory("templates");
                tmplDir = p.toFile();

                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    String filename = url.toString();
                    filename = filename.substring(filename.lastIndexOf("/") + 1);

                    File f = new File(tmplDir, filename);
                    f.deleteOnExit();

                    InputStream is = url.openConnection().getInputStream();
                    FileOutputStream os = new FileOutputStream(f);

                    byte[] buffer = new byte[4096];

                    int n;
                    while(-1 != (n = is.read(buffer))) {
                        os.write(buffer, 0, n);
                    }

                    os.flush();
                    os.close();
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace(); //FIXME
            }
        }
        return tmplDir;
    }
}
