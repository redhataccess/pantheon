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

        // Find the included file relative to the current resource's location
        Resource includeResource = resolver.getResource(this.resource.getParent(), target);
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
}
