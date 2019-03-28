package com.redhat.pantheon.asciidoctor.extension;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;

import java.util.Map;

public class SlingResourceIncludeProcessor extends IncludeProcessor {

    private ResourceResolver resolver;
    private Resource resource;

    @Override
    public boolean handles(String target) {
        return true;
    }

    @Override
    public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {

        // Find the included file relative to the current resource's location
        Resource parent = resource.getParent();
        Resource includeResource = resolver.getResource(parent, target);
        // Odds are good that our fist attempt was looking for something like "include.adoc", but our resource was
        // simply named "include", so try again after dropping the suffix.
        if (includeResource == null && target.contains(".")) {
            includeResource = resolver.getResource(parent, target.substring(0, target.lastIndexOf('.')));
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
        }

        reader.push_include(content, target, target, 1, attributes);
    }

    public void setContext(ResourceResolver resolver, Resource resource) {
        this.resolver = resolver;
        this.resource = resource;
    }
}
