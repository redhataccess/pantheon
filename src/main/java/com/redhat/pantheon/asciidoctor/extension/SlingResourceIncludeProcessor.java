package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.Module;
import com.redhat.pantheon.model.api.SlingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SlingResourceIncludeProcessor extends IncludeProcessor {

    private final Logger log = LoggerFactory.getLogger(SlingResourceIncludeProcessor.class);

    private ResourceResolver resolver;
    private Resource parent;

    public SlingResourceIncludeProcessor(final Resource resource) {
        this.resolver = resource.getResourceResolver();
        this.parent = resource.getParent();
    }

    @Override
    public boolean handles(String target) {
        return true;
    }

    @Override
    public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
        log.trace("Attempting to include {}", target);
        log.trace("Parent: {}", parent);
        log.trace("reader.getDir(): {}", reader.getDir());
        log.trace("reader.getFile(): {}", reader.getFile());

        String fixedTarget = target;
        if (reader.getFile().isEmpty()) {
            log.trace("At top level, no need to fix target");
        } else {
            fixedTarget = reader.getDir() + "/" + target;
            log.trace("Fixed target: " + fixedTarget);
        }
        Resource includeResource = resolver.getResource(parent, fixedTarget);
        String content = "Invalid include: " + target;

        if(includeResource != null) {
            SlingResource sIncludeResource = includeResource.adaptTo(SlingResource.class);

            // Included resource might be a plain file or another module
            if( sIncludeResource.getProperty("jcr:primaryType", String.class).equals("pant:module") ) {
                Module module = sIncludeResource.adaptTo(Module.class);
                // TODO, right now only default locale and latest version of the module are used
                content = module.locales.get()
                        .getModuleLocale(GlobalConfig.DEFAULT_MODULE_LOCALE)
                        .revisions.get()
                        .getDefaultRevision()
                        .asciidocContent.get();
            } else {
                // It's a plain file
                FileResource file = sIncludeResource.adaptTo(FileResource.class);
                content = file.jcrContent.get()
                        .jcrData.get();
            }
        } else {
            log.warn("Could not find include for {}", target);
        }

        reader.push_include(content, target, target, 1, attributes);
    }
}
