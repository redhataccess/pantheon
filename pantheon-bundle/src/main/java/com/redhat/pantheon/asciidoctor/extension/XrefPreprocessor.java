package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.assembly.TableOfContents;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentLocale;
import com.redhat.pantheon.model.document.DocumentVariant;
import org.apache.sling.api.resource.Resource;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XrefPreprocessor extends Preprocessor {

    private static final Logger log = LoggerFactory.getLogger(XrefPreprocessor.class);

    private static final Pattern XREF_PATTERN = Pattern.compile("xref:(?<filepath>\\S*?)(?:#(?<anchor>\\S*))?\\[(?<label>.*?)\\]");
    private static final Pattern TRIANGLE_PATTERN = Pattern.compile("<<(.*?),(.*?)>>"); // TODO

    private DocumentVariant documentVariant;
    private TableOfContents toc;
    private String newModulePath;

    public XrefPreprocessor(DocumentVariant documentVariant, TableOfContents tableOfContents) {
        this.documentVariant = documentVariant;
        this.toc = tableOfContents;
    }

    @Override
    public void process(org.asciidoctor.ast.Document adocDocument, PreprocessorReader reader) {
        List<String> lines = reader.readLines();
        List<String> output = new ArrayList<>();

        Set<String> modulePaths = new HashSet<>();
        for (TableOfContents.Entry entry : toc.getEntries()) {
            modulePaths.add(entry.getModule().getPath());
        }

        for (String line : lines) {
            Matcher matcher = XREF_PATTERN.matcher(line);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String originalTarget = matcher.group("filepath");
                // Assume it's a relative path to a file in the same repo for now
                Resource desiredTarget = documentVariant.getResourceResolver().getResource(documentVariant.getParentLocale().getParent().getParent().getPath() + "/" + originalTarget);
                if (desiredTarget == null) {
                    // Can't tell what the author is trying to link to, just leave the xref alone and hope for the best
                    // TODO - plug in a validation warning/error here once validation is a thing
                    matcher.appendReplacement(sb, matcher.group(0));
                } else {
                    if (!PantheonConstants.RESOURCE_TYPE_ASSEMBLY.equals(desiredTarget.getResourceType())
                            && !PantheonConstants.RESOURCE_TYPE_MODULE.equals(desiredTarget.getResourceType())) {
                        // TODO - is this part of the if/else really necessary?
                        matcher.appendReplacement(sb, matcher.group(0));
                    } else {
                        Document docTarget = desiredTarget.adaptTo(Document.class);
                        StringBuilder replacement = new StringBuilder("xref:");
                        String anchor = matcher.group("anchor");

                        // If we enter this if-block, then we assume that we are linking OUTSIDE of this doc.
                        // For example, from one standlone module to another standalone module.
                        // If we DO NOT enter this if-block, then we assume that we are linking INSIDE of this doc.
                        // For example, from one module inside an assembly to another module inside the same assembly.
                        if (!modulePaths.contains(docTarget.getPath())) {
                            String targetUuid = docTarget
                                    .child(documentVariant.getParentLocale().getName(), DocumentLocale.class).get() // TODO - assume same locale for now
                                    .variants().get()
                                    .variant(documentVariant.getName()).get() // TODO - assume same variant for now
                                    .uuid().get();

                            replacement.append(targetUuid);
                        } else if (anchor == null) {
                            // If we land here, it means:
                            // 1.) We are linking INSIDE of this document
                            // 2.) The user did not specify an anchor.
                            // How does this happen? Answer: when you're building an assembly, and the user xrefs from
                            // one module to another module via filepath only. In this scenario, we link them to the
                            // beginning of the module, which is an anchor that is injected by the include processor.
                            anchor = "_" + docTarget.uuid().get();
                        }

                        replacement.append("#")
                                .append(anchor == null ? "" : anchor)
                                .append("[")
                                .append(matcher.group("label"))
                                .append("]");

                        matcher.appendReplacement(sb, replacement.toString());
                    }
                }
            }
            matcher.appendTail(sb);
            output.add(sb.toString());
        }

        reader.restoreLines(output);
    }
}