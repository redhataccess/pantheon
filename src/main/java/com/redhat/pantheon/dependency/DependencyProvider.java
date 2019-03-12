package com.redhat.pantheon.dependency;

import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import org.asciidoctor.Asciidoctor;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by ben on 3/12/19.
 */
public abstract class DependencyProvider {

    private Asciidoctor asciidoctor;
    private SlingResourceIncludeProcessor includeProcessor = new SlingResourceIncludeProcessor();

    public Asciidoctor getAsciidoctor() throws IOException {
        if (asciidoctor == null) {
            asciidoctor = Asciidoctor.Factory.create(getGemPaths());
            asciidoctor.javaExtensionRegistry().includeProcessor(includeProcessor);
        }
        return asciidoctor;
    }

    public SlingResourceIncludeProcessor getIncludeProcessor() {
        return includeProcessor;
    }

    public abstract List<String> getGemPaths() throws IOException;

    public abstract File getTemplateDir() throws IOException;
}
