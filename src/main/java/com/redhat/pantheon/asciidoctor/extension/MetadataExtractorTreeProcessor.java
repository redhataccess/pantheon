package com.redhat.pantheon.asciidoctor.extension;

import com.google.common.collect.Streams;
import com.redhat.pantheon.model.module.Metadata;
import com.redhat.pantheon.util.function.FunctionalUtils;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;

import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Streams.concat;
import static com.google.common.collect.Streams.stream;
import static java.util.Optional.of;

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

    private final Metadata metadata;

    public MetadataExtractorTreeProcessor(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Document process(Document document) {
        extractDocTitle(document);
        extractHeadline(document);
        return document;
    }

    /**
     * Extracts the document title from an asciidoc document
     * @param document
     */
    private void extractDocTitle(Document document) {
        String docTitle = document.getDoctitle();
        if(!isNullOrEmpty(docTitle)) {
            metadata.title.set(docTitle);
        }
    }

    /**
     * Extracts the document's headline from an asciidoc document.
     * The headline is the first second-level header in the document (if one is present).
     * @param document
     */
    private void extractHeadline(Document document) {
        // Get the first section (that's where a subtitle will be)
        Optional<StructuralNode> headlineBlock = nodeFlatMap(document)
                // find section blocks with level == 1
                .filter(block -> {
                    try {
                        return block.getContext().equals("section") && block.getLevel() == 1;
                    } catch (Exception e) {
                        // Asciidoctor (the Ruby code) throws certain exceptions when properties are not available.
                        // In this case 'context' might not be available, so in that case the filter should just
                        // return false
                        return false;
                    }
                })
                .findFirst();
        headlineBlock.ifPresent(headline -> metadata.headline.set(headline.getTitle()));

        // if no headline is detected, reset it
        if(!headlineBlock.isPresent()) {
            metadata.headline.set(null);
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
