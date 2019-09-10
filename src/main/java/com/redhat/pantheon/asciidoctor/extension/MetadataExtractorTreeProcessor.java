package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.module.Metadata;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;

import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

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
        extractAbstract(document);
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
     * Extracts the document's abstract from an asciidoc document.
     * The abstract is assumed to be the first paragraph of text in the document.
     * @param document
     */
    private void extractAbstract(Document document) {
        Optional<String> abstractContent = document.getBlocks().stream()
                // find the first content block
                .findFirst()
                // only the first paragraph is allowed, all other blocks are ignored
                .filter(block -> block.getContext().equals("paragraph"))
                // make sure it has some content
                .filter(contentBlock -> !isNullOrEmpty(contentBlock.getContent().toString()))
                // get the content itself
                .map(contentBlock -> contentBlock.getContent().toString());
        abstractContent.ifPresent(content -> metadata.mAbstract.set(content));

        // If no abstract is detected, reset it
        if(!abstractContent.isPresent()) {
            metadata.mAbstract.set(null);
        }
    }

    /**
     * Extracts the document's headline from an asciidoc document.
     * The headline is the first second-level header in the document (if one is present).
     * @param document
     */
    private void extractHeadline(Document document) {
        // Get the first section (that's where a subtitle will be)
        Optional<StructuralNode> headlineBlock = document.getBlocks().stream()
                // find section blocks with level == 1
                .filter(block -> block.getContext().equals("section") && block.getLevel() == 1)
                .findFirst();
        headlineBlock.ifPresent(headline -> metadata.headline.set(headline.getTitle()));

        // if no headline is detected, reset it
        if(!headlineBlock.isPresent()) {
            metadata.headline.set(null);
        }
    }
}
