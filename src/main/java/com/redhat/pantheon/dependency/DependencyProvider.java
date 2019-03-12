package com.redhat.pantheon.dependency;

import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import org.asciidoctor.Asciidoctor;

import java.io.File;
import java.util.List;

/**
 * Created by ben on 3/12/19.
 */
public abstract class DependencyProvider {

    private Asciidoctor asciidoctor;
    private SlingResourceIncludeProcessor includeProcessor = new SlingResourceIncludeProcessor();

    public Asciidoctor getAsciidoctor() {
        if (asciidoctor == null) {
            System.out.println("asciidoctor initializing...");

            asciidoctor = Asciidoctor.Factory.create(getGemPaths());
            asciidoctor.javaExtensionRegistry().includeProcessor(includeProcessor);
            System.out.println("asciidoctor initialized");
        }
        return asciidoctor;
    }

    public SlingResourceIncludeProcessor getIncludeProcessor() {
        return includeProcessor;
    }

    public abstract List<String> getGemPaths();

    public abstract File getTemplateDir();
}
