package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.ModuleRevision;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;

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
 * @author Carlos Munoz
 */
public class MetadataExtractorTreeProcessor extends Treeprocessor {

    private final ModuleRevision moduleRevision;

    public MetadataExtractorTreeProcessor(ModuleRevision moduleRevision) {
        this.moduleRevision = moduleRevision;
    }

    @Override
    public Document process(Document document) {
        extractDocTitle(document);
        extractAbstract(document);
        return document;
    }

    private void extractDocTitle(Document document) {
        String docTitle = document.getDoctitle();
        if(!isNullOrEmpty(docTitle)) {
            moduleRevision.title.set(docTitle);
        }
    }

    private void extractAbstract(Document document) {
        // Abstract is the first paragraph
        if(document.getBlocks().size() > 0) {
            StructuralNode firstBlock = document.getBlocks().get(0);
            if(firstBlock.getContent() != null) {
                String abstractContent = firstBlock.getContent().toString();
                if (!isNullOrEmpty(abstractContent)) {
                    moduleRevision.mAbstract.set(abstractContent);
                }
            }
        }
    }
}
