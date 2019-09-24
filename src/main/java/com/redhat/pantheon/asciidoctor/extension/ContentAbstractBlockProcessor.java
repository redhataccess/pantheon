package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.module.Metadata;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Contexts;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Reader;

import java.util.Map;

import static org.asciidoctor.ast.ContentModel.SIMPLE;
import static org.asciidoctor.extension.Contexts.PARAGRAPH;

/**
 * A block processor which extracts the abstract from an asciidoc file.
 * A paragraph must be given the named attribute called 'pantheon:abstract' for the system
 * to record it as the abstract in the metadata, like so:
 *
 * [..., pantheon:abstract, ...]
 * This is the abstract content
 *
 * If multiple paragraphs are marked with this attribute in a single module, there is no
 * guarantee any of them will be chosen as the abstract, although one of them will. This means
 * only one paragraph should be marked with this attribute.
 */
@Name("pantheon:abstract")
@Contexts(PARAGRAPH)
@ContentModel(SIMPLE)
public class ContentAbstractBlockProcessor extends BlockProcessor {

    private final Metadata metadata;

    public ContentAbstractBlockProcessor(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        metadata.mAbstract.set(reader.read());
        // NOTE This extension should only be used for metadata extraction and NOT during content generation;
        //  there is no need to create a block
        return null;
    }
}
