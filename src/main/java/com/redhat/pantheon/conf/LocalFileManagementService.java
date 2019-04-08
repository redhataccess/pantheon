package com.redhat.pantheon.conf;

import org.apache.commons.io.FileUtils;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component(
        service = LocalFileManagementService.class,
        scope = ServiceScope.SINGLETON
)
public class LocalFileManagementService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileManagementService.class);

    private File templateDirectory;

    private List<String> gemPaths;

    @Activate
    public void activate() throws IOException {
        initializeTemplateDirectories();
        initializeGemPaths();
    }

    public void initializeTemplateDirectories() throws IOException {
        Enumeration<URL> urls = FrameworkUtil.getBundle(LocalFileManagementService.class)
                .findEntries("apps/pantheon/templates/haml/html5", "*", false);
        Path p = Files.createTempDirectory("templates");

        log.info("Initializing template directories at " + p.toString());

        if (urls != null && urls.hasMoreElements()) {
            templateDirectory = p.toFile();
        }

        while (urls != null && urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String filename = url.toString();
            filename = filename.substring(filename.lastIndexOf("/") + 1);

            File f = new File(templateDirectory, filename);
            f.deleteOnExit();

            InputStream is = url.openConnection().getInputStream();

            FileUtils.copyInputStreamToFile(is, f);
        }
    }

    public File getTemplateDirectory() {
        if (templateDirectory == null) {
            log.warn("LocalFileManagementService.getTemplateDirectory() is returning a null value. If this is " +
                    "running in a Red Hat environment, this is ALMOST CERTAINLY a project misconfiguration. Check " +
                    "to ensure that there are no broken symlinks in your build environment.");
        }
        return templateDirectory;
    }

    public List<String> getGemPaths() {
        return gemPaths;
    }

    private void initializeGemPaths() {
        Enumeration<URL> gems = FrameworkUtil.getBundle(LocalFileManagementService.class)
                .findEntries("gems", "*", false);
        List<String> list = new ArrayList<>();
        while (gems.hasMoreElements()) {
            URL g = gems.nextElement();
            list.add("uri:classloader:" + g.getPath() + "lib");
        }
        gemPaths = Collections.unmodifiableList(list);
    }

}
