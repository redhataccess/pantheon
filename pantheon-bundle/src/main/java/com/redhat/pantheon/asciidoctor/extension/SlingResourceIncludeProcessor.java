package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.helper.Symlinks;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.assembly.TableOfContents;
import com.redhat.pantheon.model.document.SourceContent;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleLocale;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.redhat.pantheon.helper.PantheonConstants.ADOC_LEVELOFFSET;
import static com.redhat.pantheon.helper.PantheonConstants.JCR_TYPE_MODULE;
import static com.redhat.pantheon.helper.PantheonConstants.MACRO_INCLUDE;
import static com.redhat.pantheon.model.api.util.ResourceTraversal.traverseFrom;
import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;

public class SlingResourceIncludeProcessor extends IncludeProcessor {

    private final Logger log = LoggerFactory.getLogger(SlingResourceIncludeProcessor.class);

    private final ResourceResolver resolver;
    private final Resource parent;
    private final TableOfContents toc;

    public SlingResourceIncludeProcessor(Resource resource, TableOfContents toc) {
        this.resolver = resource.getResourceResolver();
        this.parent = resource.getParent();
        this.toc = toc;
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
            if (includedResourceAsModel.field(JCR_PRIMARYTYPE, String.class).get().equals(JCR_TYPE_MODULE)) {
                Module module = includedResourceAsModel.adaptTo(Module.class);

                // TODO, right now only default locale and latest (draft) version of the module are used
                content = traverseFrom(module)
                        .toChild(module1 -> module.locale(GlobalConfig.DEFAULT_MODULE_LOCALE))
                        .toChild(ModuleLocale::source)
                        .toChild(SourceContent::draft)
                        .toChild(FileResource::jcrContent)
                        .toField(FileResource.JcrContent::jcrData)
                        .get();

                String documentLeveloffset = (String) document.getAttribute(ADOC_LEVELOFFSET);
                int originalOffset = getInteger(documentLeveloffset);
                // This next line is important - it fixes an asciidoctor glitch. If you have a preprocessor doing
                // *anything at all* as part of your build, then a leveloffset brought in as an include parameter is
                // injected directly as a document-wide attribute by asciidoctor. However, the logic that performs that
                // is flawed. We have to remove the attribute from the map and handle it ourselves to work around the
                // bug.
                String attributeLeveloffset = (String) attributes.remove(ADOC_LEVELOFFSET);
                int effectiveOffset = getOffset(originalOffset, attributeLeveloffset);

                StringBuilder finalContent = new StringBuilder();
                finalContent.append(":pantheon-leveloffset: {leveloffset}\r\n");

                if (attributeLeveloffset != null) {
                    finalContent.append(":leveloffset: ").append(attributeLeveloffset).append("\r\n");
                }
                finalContent.append(MACRO_INCLUDE).append(":").append(toc.getEntries().size()).append("[]\r\n\r\n");
                toc.addEntry(effectiveOffset, module);

                finalContent.append(":pantheon_module_id: ")
                        .append(module.uuid().get())
                        .append("\r\n")
                        .append("[[_")
                        .append(module.uuid().get())
                        .append("]]\r\n")
                        .append(content)
                        .append("\r\n")
                        .append(":!pantheon_module_id:")
                        .append("\r\n");
                finalContent.append(":leveloffset: {pantheon-leveloffset}\r\n");
                finalContent.append(":!pantheon-leveloffset:\r\n");

                content = finalContent.toString();
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

    private int getOffset(int leveloffsetDocument, String leveloffsetAttribute) {
        // Don't need to worry about relative vs absolute values for document level because asciidoctor evaluates that
        // on our behalf
        if (leveloffsetAttribute == null) {
            return leveloffsetDocument;
        }

        boolean relative = leveloffsetAttribute.startsWith("+") || leveloffsetAttribute.startsWith("-");
        if (relative) {
            return leveloffsetDocument + getInteger(leveloffsetAttribute.substring(1));
        } else {
            return getInteger(leveloffsetAttribute);
        }
    }

    private Resource resolveWithSymlinks(String path, Resource pathParent) {

        Resource resource = Symlinks.resolve(resolver, pathParent.getPath() + "/" + path);
        if ("sling:nonexisting".equals(resource.getResourceType())) {
            return null;
        }
        return resource;
    }
}
