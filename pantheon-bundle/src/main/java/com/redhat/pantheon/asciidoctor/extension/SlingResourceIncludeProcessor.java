package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.api.FileResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;

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
            SlingModel includedResourceAsModel = includeResource.adaptTo(SlingModel.class);

            // Included resource might be a plain file or another module
            if( includedResourceAsModel.getProperty(JCR_PRIMARYTYPE, String.class).equals("pant:module") ) {
                Module module = includedResourceAsModel.adaptTo(Module.class);
                // TODO, right now only default locale and latest (draft) version of the module are used
                content = module.getDraftContent(GlobalConfig.DEFAULT_MODULE_LOCALE)
                            .get()
                            .asciidocContent().get();
            } else {
                // It's a plain file
                // TODO Resources (assets) will be versioned too, and module versions will have a record of their
                // TODO specific asset version, so this extension will need to fetch the correct one
                FileResource file = includedResourceAsModel.adaptTo(FileResource.class);
                content = file.jcrContent().get()
                        .jcrData().get();
            }
        } else {
            log.warn("Could not find include for {}", target);
        }

        reader.push_include(content, target, target, 1, attributes);
    }
}
