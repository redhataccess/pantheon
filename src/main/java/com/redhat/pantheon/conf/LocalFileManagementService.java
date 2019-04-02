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
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

@Component(
        service = LocalFileManagementService.class,
        scope = ServiceScope.SINGLETON
)
public class LocalFileManagementService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileManagementService.class);

    private Optional<File> templateDirectory = Optional.empty();

    private Optional<List<String>> gemPaths = Optional.empty();

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

        templateDirectory = Optional.of(p.toFile());

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String filename = url.toString();
            filename = filename.substring(filename.lastIndexOf("/") + 1);

            File f = new File(templateDirectory.get(), filename);
            f.deleteOnExit();

            InputStream is = url.openConnection().getInputStream();

            FileUtils.copyInputStreamToFile(is, f);
        }
    }

    public synchronized File getTemplateDirectory() {
        if (!templateDirectory.isPresent()) {
            try {
                initializeTemplateDirectories();
            } catch (IOException e) {
                log.error("Error initializing template directory", e);
            }
        }
        return templateDirectory.get();
    }

    public synchronized List<String> getGemPaths() throws IOException {
        if (!gemPaths.isPresent()) {
            initializeGemPaths();
        }
        return gemPaths.get();
    }

    private void initializeGemPaths() {
        Enumeration<URL> gems = FrameworkUtil.getBundle(LocalFileManagementService.class)
                .findEntries("gems", "*", false);
        gemPaths = Optional.of(new ArrayList<>());
        while (gems.hasMoreElements()) {
            URL g = gems.nextElement();
            gemPaths.get().add("uri:classloader:" + g.getPath() + "lib");
        }
    }

}
