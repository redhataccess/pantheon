package com.redhat.pantheon.conf;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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

    private List<String> gemPaths;

    @Activate
    public void activate() throws IOException {
        initializeTemplateDirectories();
        initializeGemPaths();
    }

    public void initializeTemplateDirectories() throws IOException {
        Optional<Enumeration<URL>> urls = Optional.ofNullable(FrameworkUtil.getBundle(LocalFileManagementService.class)
                .findEntries("apps/pantheon/templates/haml/html5", "*", false));
        Path p = Files.createTempDirectory("templates");

        log.info("Initializing template directories at " + p.toString());

        if (urls.isPresent()) {
            templateDirectory = Optional.of(p.toFile());
        }

        Collections.list(urls.orElse(Collections.emptyEnumeration()))
                .stream()
                .map(url -> Pair.of(url, url.toString()))
                .map(pair -> Pair.of(pair.getLeft(), pair.getRight().substring(pair.getRight().lastIndexOf("/") + 1)))
                .map(pair -> Pair.of(pair.getLeft(), new File(templateDirectory.get(), pair.getRight())))
                .peek(pair -> pair.getRight().deleteOnExit())
                .map(pair -> {
                    try {
                        return Pair.of(pair.getLeft().openConnection().getInputStream(), pair.getRight());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .forEach(pair -> {
                    try {
                        FileUtils.copyInputStreamToFile(pair.getLeft(), pair.getRight());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    public Optional<File> getTemplateDirectory() {
        if (!templateDirectory.isPresent()) {
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
