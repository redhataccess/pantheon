package com.redhat.pantheon.asciidoctor.extension;

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
    private Resource resource;

    public SlingResourceIncludeProcessor(final Resource resource) {
        this.resolver = resource.getResourceResolver();
        this.resource = resource;
    }

    @Override
    public boolean handles(String target) {
        return true;
    }

    @Override
    public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
        log.trace("Attempting to include {}", target);
        log.trace("Resource: {}", resource);

        // Find the included file relative to the current resource's location
        Resource parent = resource.getParent();
        log.trace("Resource parent: {}", parent);
        Resource includeResource = resolver.getResource(parent, target);
        // Odds are good that our fist attempt was looking for something like "include.adoc", but our resource was
        // simply named "include", so try again after dropping the suffix.
        if (includeResource == null && target.contains(".")) {
            String newTarget = target.substring(0, target.lastIndexOf('.'));
            log.trace("Could not find {}, searching for {}", target, newTarget);
            includeResource = resolver.getResource(parent, newTarget);
        }
        String content = "Invalid include: " + target;

        if(includeResource != null) {
            // Included resource might be a plain file or another module
            if( includeResource.getChild("asciidoc") != null ) {
                content = includeResource.getChild("asciidoc")
                    .getChild("jcr:content")
                    .getValueMap()
                    .get("jcr:data", String.class);
            } else {
                // It's a plain file
                content = includeResource
                    .getChild("jcr:content")
                    .getValueMap()
                    .get("jcr:data", String.class);
            }
        } else {
            log.warn("Could not find include for {}", target);
        }

        reader.push_include(content, target, target, 1, attributes);
    }
}
