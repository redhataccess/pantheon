package com.redhat.pantheon.sling;

import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import org.asciidoctor.Asciidoctor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by ben on 3/7/19.
 */
public class PantheonBundle implements BundleActivator {

    private static Asciidoctor asciidoctor;
    private static SlingResourceIncludeProcessor includeProcessor = new SlingResourceIncludeProcessor();
    private static File templateDir;

    @Override
    public void start(BundleContext bundleContext) throws Exception {


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
            while(-1 != (n = is.read(buffer))) {
                os.write(buffer, 0, n);
            }

            os.flush();
            os.close();
            is.close();
        }

        System.out.println("Pantheon Bundle activated");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        asciidoctor.shutdown();
    }

    public static Asciidoctor getAsciidoctor() {
        if (asciidoctor == null) {
            Enumeration<URL> gems = FrameworkUtil.getBundle(PantheonBundle.class).findEntries("gems", "*", false);
            List<String> gemPaths = new ArrayList<>();
            while (gems.hasMoreElements()) {
                URL g = gems.nextElement();
                gemPaths.add("uri:classloader:" + g.getPath() + "lib");
            }

            asciidoctor = Asciidoctor.Factory.create(gemPaths);
            asciidoctor.javaExtensionRegistry().includeProcessor(includeProcessor);
        }
        return asciidoctor;
    }

    public static SlingResourceIncludeProcessor getIncludeProcessor() {
        return includeProcessor;
    }

    public static File getTemplateDir() {
        return templateDir;
    }
}
