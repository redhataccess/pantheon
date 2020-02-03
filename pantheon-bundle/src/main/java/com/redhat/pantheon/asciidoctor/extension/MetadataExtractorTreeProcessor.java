package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.model.module.ModuleType;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Streams.concat;
import static com.google.common.collect.Streams.stream;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

/**
 * A tree processor that extracts metadata from the asciidoc AST and inserts it
 * into a sling resource.
 *
 * <p>
 * Extracted items include:
 * <ul>
 *     <li>Title - the asciidoc document's title</li>
 *     <li>Abstract - The first paragraph in the asciidoc document</li>
 * </ul>
 *
 * A current restriction is that when the extracted metadata contains variable
 * substitutions, the substitutions will not be applied when recording the
 * value in the resource.
 * </p>
 *
 * <p>
 * For more information about how to interpret and extract data from the asciidoctor
 * AST, see:
 * https://github.com/asciidoctor/asciidoctorj/blob/asciidoctorj-1.6.0/docs/integrator-guide.adoc#understanding-the-ast-classes
 * </p>
 *
 * @author Carlos Munoz
 */
public class MetadataExtractorTreeProcessor extends Treeprocessor {

    private final static String MODULE_TYPE_ATT_NAME = "pantheon-module-type";

    private final Logger log = LoggerFactory.getLogger(MetadataExtractorTreeProcessor.class);

    private final Metadata metadata;

    public MetadataExtractorTreeProcessor(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Document process(Document document) {
        List<StructuralNode> allNodes = nodeFlatMap(document).collect(toList());
        extractDocTitle(document);
        extractHeadline(allNodes);
        extractAbstract(allNodes);
        extractModuleType(document);
        return document;
    }

    /**
     * Extracts the document title from an asciidoc document
     * @param document
     */
    private void extractDocTitle(Document document) {
        String docTitle = document.getDoctitle();
        if(!isNullOrEmpty(docTitle)) {
            metadata.title().set(docTitle);
        }
    }

    /**
     * Extracts the document's headline from an asciidoc document.
     * The headline is the first second-level header in the document (if one is present).
     */
    private void extractHeadline(List<StructuralNode> allNodes) {
        // Get the first section (that's where a subtitle will be)
        Optional<StructuralNode> headlineBlock = allNodes.stream()
                // find section blocks with level == 1
                .filter(block -> {
                    try {
                        return block.getContext().equals("section") && block.getLevel() == 1;
                    } catch (Exception e) {
                        // Asciidoctor (the Ruby code) throws certain exceptions when properties are not available.
                        // In this case 'context' might not be available, and so the filter should just
                        // return false
                        return false;
                    }
                })
                .findFirst();
        headlineBlock.ifPresent(headline -> metadata.headline().set(headline.getTitle()));

        // if no headline is detected, reset it
        if(!headlineBlock.isPresent()) {
            metadata.headline().set(null);
        }
    }

    /**
     * Extracts the document's abstract.
     * The abstract is the first found block with the 'abstract' role.
     */
    private void extractAbstract(List<StructuralNode> allNodes) {
        Optional<StructuralNode> abstractNode =
                allNodes.stream()
                        .filter(block -> {
                            try {
                                return block.getRoles().contains("abstract");
                            } catch (Exception e) {
                                // Asciidoctor (the Ruby code) throws certain exceptions when properties are not available.
                                // In this case 'context' might not be available, and so the filter should just
                                // return false
                                return false;
                            }
                        })
                        .findFirst();
        abstractNode.ifPresent(node -> metadata.mAbstract().set(node.getContent().toString()));

        // if no abstract is detected, reset if
        if(!abstractNode.isPresent()) {
            metadata.mAbstract().set(null);
        }
    }

    /**
     * Extract the module type from the asciidoc content. This method looks at the pantheon-module-type
     * property defined in the module.
     * @param document The document which is being parsed by the extension
     * @see ModuleType for the valid values for the property.
     */
    private void extractModuleType(Document document) {
        Object attValue = document.getAttribute(MODULE_TYPE_ATT_NAME);
        if(attValue != null) {
            try {
                ModuleType moduleType = ModuleType.valueOf(attValue.toString());
                metadata.moduleType().set(moduleType);
            } catch (IllegalArgumentException e) {
                metadata.moduleType().set(null);
                log.warn("Invalid argument for " + MODULE_TYPE_ATT_NAME + " asciidoc attribute: "
                    + attValue.toString());
            }
        }
        else {
            metadata.moduleType().set(null);
        }
    }

    /**
     * Converts a document into a flat representation of the nodes making it up.
     * This method should only be called once per document processing, as it can be rather expensive.
     * @param document The document which nodes' are being flat mapped
     * @return A stream of nodes making up the given document, in order of appearance
     */
    private static Stream<StructuralNode> nodeFlatMap(Document document) {
        return document.getBlocks().stream()
                .flatMap(node -> concat(  stream(of(node)), node.getBlocks().stream()));
    }
}
