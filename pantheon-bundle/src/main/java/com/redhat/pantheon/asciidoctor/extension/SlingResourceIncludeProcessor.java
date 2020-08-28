package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.helper.Symlinks;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.assembly.TableOfContents;

import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleLocale;
import com.redhat.pantheon.model.document.SourceContent;
import com.redhat.pantheon.model.module.ModuleVariant;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;
import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;

public class SlingResourceIncludeProcessor extends IncludeProcessor {

    private final Logger log = LoggerFactory.getLogger(SlingResourceIncludeProcessor.class);

    private final ResourceResolver resolver;
    private final Resource parent;
    private final TableOfContents toc = new TableOfContents();

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

        if (includeResource == null) {
            //Check if maybe there are symlinks?
            includeResource = resolveWithSymlinks(fixedTarget, parent);
        }

        if (includeResource != null) {
            SlingModel includedResourceAsModel = includeResource.adaptTo(SlingModel.class);

            // Included resource might be a plain file or another module
            if (includedResourceAsModel.field(JCR_PRIMARYTYPE, String.class).get().equals("pant:module")) {
                Module module = includedResourceAsModel.adaptTo(Module.class);
                ModuleVariant moduleVariant = traverseFrom(module)
                        .toChild(m -> m.locale(DEFAULT_MODULE_LOCALE))
                        .toChild(ModuleLocale::variants)
                        .toChild(variants -> variants.variant(module.getWorkspace().getCanonicalVariantName()))
                        .get();
                int finalOffset = getOffset(document, attributes);
                toc.addEntry(finalOffset, moduleVariant);

                // TODO, right now only default locale and latest (draft) version of the module are used
                content = traverseFrom(moduleVariant.getParent().getParent())
                        .toChild(ModuleLocale::source)
                        .toChild(SourceContent::draft)
                        .toChild(FileResource::jcrContent)
                        .toField(FileResource.JcrContent::jcrData)
                        .get();
                content = new StringBuilder()
                        .append(":pantheon_module_id: ")
                        .append(moduleVariant.uuid().get())
                        .append("\r\n")
                        .append("[[_")
                        .append(moduleVariant.uuid().get())
                        .append("]]\r\n")
                        .append(content)
                        .append("\r\n")
                        .append(":!pantheon_module_id:")
                        .append("\r\n")
                        .toString();
            } else {
                // It's a plain file
                FileResource file = includedResourceAsModel.adaptTo(FileResource.class);
                content = file.jcrContent().get()
                        .jcrData().get();
            }
        } else {
            log.warn("Could not find include for {}", target);
        }

        reader.push_include(content, target, target, 1, attributes);
    }

    private int getInteger(String str) {
        try {
            return Optional.ofNullable(str).map(s -> Integer.parseInt(s)).orElse(0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int getOffset(Document document, Map<String, Object> attributes) {
        // Don't need to worry about relative vs absolute values here because asciidoctor evaluates that on our behalf
        int docOffset = getInteger((String) document.getAttribute("leveloffset"));

        String offsetParam = (String) attributes.get("leveloffset");
        if (offsetParam == null) {
            return docOffset;
        }

        boolean relative = offsetParam.startsWith("+") || offsetParam.startsWith("-");
        if (relative) {
            return docOffset + getInteger(offsetParam.substring(1));
        } else {
            return getInteger(offsetParam);
        }
    }

    private Resource resolveWithSymlinks(String path, Resource pathParent) {

        Resource resource = Symlinks.resolve(resolver, pathParent.getPath() + "/" + path);
        if ("sling:nonexisting".equals(resource.getResourceType())) {
            return null;
        }
        return resource;
    }

    public TableOfContents getTableOfContents() {
        return toc;
    }
}
