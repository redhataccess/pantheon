package com.redhat.pantheon.asciidoctor.extension;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;

import java.util.Map;

public class PantheonXrefTargetProcessor extends InlineMacroProcessor {

    public static final String MACRO_PREFIX = "pantheon-xref-target";

    @Override
    public Object process(ContentNode contentNode, String s, Map<String, Object> map) {
        return "<div id=\"" + s + "\" class=\"pantheon-anchor-div\"></div>";
    }
}
