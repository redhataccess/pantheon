package com.redhat.pantheon.conf;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.osgi.framework.Bundle;
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
import java.util.*;

import static com.redhat.pantheon.util.function.FunctionalUtils.tryAndThrowRuntime;
import static java.util.Optional.*;

/**
 * Stores all global configuration values. Since some values are better to load at system start-up,
 * this class provides a method to eagerly load any such values.
 *
 * @author Carlos Munoz
 */
@Component(
        service = GlobalConfig.class,
        scope = ServiceScope.SINGLETON
)
public class GlobalConfig {

    private static final Logger log = LoggerFactory.getLogger(GlobalConfig.class);

    public static final Locale DEFAULT_MODULE_LOCALE = Locale.US;

    public static final String CONTENT_TYPE = "module";

    public static final String IMAGE_PATH_PREFIX = "/imageassets";

    private final LoadableValue<List<String>> GEM_PATHS = new LoadableValue<>(this::loadGemPaths);

    private final LoadableValue<Optional<File>> TEMPLATE_DIRECTORY =
            new LoadableValue<>(tryAndThrowRuntime(this::loadTemplateDirectory));

    /**
     * Eagerly loads any necessary configuration values.
     */
    @Activate
    protected void eagerLoad() {
        GEM_PATHS.load();
        TEMPLATE_DIRECTORY.load();
    }

    protected Bundle getCurrentBundle() {
        return FrameworkUtil.getBundle(GlobalConfig.class);
    }

    /**
     * Loader method for the template directory. This directory holds all the asciidoctor template
     * files used to generate html and other formats. Pantheon provides its own version of these
     * templates and this is the way to determine where they come from.
     * @return A directory containing all the asciidoctor template files
     * @throws IOException
     */
    private Optional<File> loadTemplateDirectory() throws IOException {
        Optional<Enumeration<URL>> urls = ofNullable(getCurrentBundle()
                .findEntries("apps/pantheon/templates/haml/html5", "*", false));
        Path p = Files.createTempDirectory("templates");

        log.info("Initializing template directories at " + p.toString());

        if (urls.isPresent()) {
            Collections.list(urls.get())
                    .stream()
                    .map(url -> Pair.of(url, url.toString()))
                    .map(pair -> Pair.of(pair.getLeft(), pair.getRight().substring(pair.getRight().lastIndexOf("/") + 1)))
                    .map(pair -> Pair.of(pair.getLeft(), new File(p.toFile(), pair.getRight())))
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
            return of(p.toFile());
        } else {
            log.warn("GlobalConfig.getTemplateDirectory() is returning a null value. If this is " +
                    "running in a Red Hat environment, this is ALMOST CERTAINLY a project misconfiguration. Check " +
                    "to ensure that there are no broken symlinks in your build environment.");
            return empty();
        }
    }

    /**
     * Loader method for the Ruby Gem paths used by Pantheon to invoke asciidoctor.
     * These paths come from inside the Pantheon bundle and need to be determined only once.
     * @return a List of strings each with a path to load Ruby gems for asciidoctor.
     */
    private List<String> loadGemPaths() {
        Enumeration<URL> gems = getCurrentBundle().findEntries("gems", "*", false);
        List<String> list = new ArrayList<>();
        while (gems.hasMoreElements()) {
            URL g = gems.nextElement();
            list.add("uri:classloader:" + g.getPath() + "lib");
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns a list of paths where to find Ruby gems
     */
    public List<String> getGemPaths() {
        return GEM_PATHS.get();
    }

    /**
     * Returns a file indicating the root path where asciidoctor templates may be found
     */
    public Optional<File> getTemplateDirectory() {
        return TEMPLATE_DIRECTORY.get();
    }
}
