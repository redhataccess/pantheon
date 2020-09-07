package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.assembly.TableOfContents;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;

import java.util.Map;

/**
 * This class exists to work around an asciidoctor bug. In short, asciidoctor handles leveloffset poorly when a
 * preprocessor is involved with the process, so this is part of the necessary mechanism to extract that information
 * at a later stage in the build process than we would otherwise have to.
 */
public class PantheonLeveloffsetProcessor extends InlineMacroProcessor {

    private TableOfContents toc;

    public PantheonLeveloffsetProcessor(TableOfContents toc) {
        this.toc = toc;
    }

    @Override
    public Object process(ContentNode contentNode, String s, Map<String, Object> map) {
        int index = Integer.valueOf(s);
        String leveloffset = (String) contentNode.getDocument().getAttribute(PantheonConstants.ADOC_LEVELOFFSET);
        int realOffset = 0;
        try {
            realOffset = Integer.valueOf(leveloffset);
        } catch (NumberFormatException e) {}
        toc.getEntries().get(index).setLevelOffset(realOffset);
        return "";
    }
}
