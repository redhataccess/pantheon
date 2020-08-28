package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.assembly.TableOfContents;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class PantheonLeveloffsetProcessorTest {

    @Test
    public void process() {
        // Given
        TableOfContents toc = new TableOfContents();
        toc.addEntry(0, null);

        ContentNode node = mock(ContentNode.class);
        Document doc = mock(Document.class);
        lenient().when(node.getDocument()).thenReturn(doc);
        lenient().when(doc.getAttribute(PantheonConstants.ADOC_LEVELOFFSET)).thenReturn("2");

        // When
        PantheonLeveloffsetProcessor proc = new PantheonLeveloffsetProcessor(toc);
        proc.process(node, "0", null);

        // Then
        assertTrue(toc.getEntries().get(0).getLevelOffset() == 2);
    }
}
