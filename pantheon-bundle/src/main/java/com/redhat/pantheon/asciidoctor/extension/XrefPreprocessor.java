package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.model.document.Document;
import org.apache.sling.api.resource.Resource;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XrefPreprocessor extends Preprocessor {

    private static final Logger log = LoggerFactory.getLogger(XrefPreprocessor.class);

    private static final Pattern XREF_PATTERN = Pattern.compile("xref:(.*?)\\[(.*?)\\]");
    private static final Pattern TRIANGLE_PATTERN = Pattern.compile("<<(.*?),(.*?)>>");

    private Document document;
    private String newModulePath;

    public XrefPreprocessor(Document document) {
        this.document = document;
    }

    @Override
    public void process(org.asciidoctor.ast.Document adocDocument, PreprocessorReader reader) {
        List<String> lines = reader.readLines();
        List<String> output = new ArrayList<>();

        for (String line : lines) {
            Matcher matcher = XREF_PATTERN.matcher(line);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String originalTarget = matcher.group(1);
                Resource desiredTarget = document.getResourceResolver().getResource(document.getParent().getPath() + "/" + originalTarget);
                if (desiredTarget == null) {
                    // Can't tell what the author is trying to link to, just leave the xref alone and hope for the best
                    // TODO - plug in a validation warning/error here once validation is a thing
                    matcher.appendReplacement(sb, matcher.group(0));
                } else {
                    matcher.appendReplacement(sb, "xref:" + "http://www.google.com" + "[" + matcher.group(2) + "]");
                }
            }
            matcher.appendTail(sb);
            output.add(sb.toString());
        }

        reader.restoreLines(output);
    }
}