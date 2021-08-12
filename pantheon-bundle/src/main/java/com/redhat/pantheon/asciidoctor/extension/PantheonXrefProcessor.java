package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.helper.Symlinks;
import com.redhat.pantheon.model.Xref;
import com.redhat.pantheon.model.assembly.TableOfContents;
import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.document.DocumentLocale;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleLocale;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.validation.helper.XrefValidationHelper;
import com.redhat.pantheon.validation.validators.XrefValidator;
import org.apache.sling.api.resource.Resource;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.redhat.pantheon.helper.PantheonConstants.RESOURCE_TYPE_ASSEMBLY;
import static com.redhat.pantheon.helper.PantheonConstants.RESOURCE_TYPE_MODULE;

public class PantheonXrefProcessor extends InlineMacroProcessor {

    public static final String MACRO_PREFIX = "pantheon-cross-reference";

    private static final Logger log = LoggerFactory.getLogger(PantheonXrefProcessor.class);

    private static final Pattern XREF_PATTERN = Pattern.compile("xref:(?<filepath>\\S*?)(?:#(?<anchor>\\S*))?\\[(?<label>.*?)\\]");
    private static final Pattern TRIANGLE_PATTERN = Pattern.compile("<<(?<filepath>\\S*?)(?:#(?<anchor>\\S*))?,(?<label>.*?)>>");
    private static final Set<String> XREF_ALLOWED_TARGET_TYPES = new HashSet<>();
    static {
        XREF_ALLOWED_TARGET_TYPES.add(RESOURCE_TYPE_MODULE);
        XREF_ALLOWED_TARGET_TYPES.add(RESOURCE_TYPE_ASSEMBLY);
    }

    private DocumentVariant documentVariant;
    private TableOfContents toc;
    private Set<String> modulePaths = null;
    private Map<String, Xref> xrefMap = new HashMap<>();

    /**
     * @param documentVariant
     * @param tableOfContents Doesn't necessarily need to be populated when this constructor is called.
     */
    public PantheonXrefProcessor(DocumentVariant documentVariant, TableOfContents tableOfContents) {
        this.documentVariant = documentVariant;
        this.toc = tableOfContents;
    }

    public String preprocess(String content) {
        List<String> urlList = new ArrayList<>();
        if (!documentVariant.getPath().startsWith("/content/docs/")) {
            content = preprocessWithPattern(content, XREF_PATTERN, urlList);
            content = preprocessWithPattern(content, TRIANGLE_PATTERN, urlList);
        }
        XrefValidationHelper.getInstance().setObjectsToValidate(urlList);
        return content;
    }

    private String preprocessWithPattern(String line, Pattern pattern, List<String> filePaths) {
        Matcher matcher = pattern.matcher(line);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String filepath = Optional.ofNullable(matcher.group("filepath")).orElse("");
            String anchor = Optional.ofNullable(matcher.group("anchor")).orElse("");
            String label = Optional.ofNullable(matcher.group("label")).orElse("");
            String pathPrefix = "";
            for (int pos = line.indexOf(matcher.group(0))-1; pos != -1; pos -=1 ) {
                pathPrefix = line.charAt(pos)+pathPrefix;
                if(pathPrefix.contains("\n")){
                    pathPrefix = pathPrefix.substring(1);
                    break;
                }
            }

            // Decide whether this is an xref that we can resolve
            // Assume it's a relative path to a file in the same repo for now
            Resource containingFolder = documentVariant.getParentLocale().getParent().getParent();
            String targetPath = containingFolder.getPath() + "/" + filepath;
            Resource desiredTarget = Symlinks.resolve(documentVariant.getResourceResolver(), targetPath);

            if (desiredTarget != null && XREF_ALLOWED_TARGET_TYPES.contains(desiredTarget.getResourceType())) {
                UUID uuid = UUID.randomUUID();
                xrefMap.put(uuid.toString(), new Xref(desiredTarget.adaptTo(Document.class), anchor, label));
                matcher.appendReplacement(sb, MACRO_PREFIX + ":" + uuid.toString() + "[]");
            } else {
                // TODO - Once validation exists, might want to add a check here for "target exists but is not publishable"
                matcher.appendReplacement(sb, matcher.group(0)); // ".group(0)" is the special group that contains the
                                                                // entire content of what was matched. I.e., we leave
                                                                // this alone/unmodified.
                if(!pathPrefix.matches("^(\\s*\\/\\/[^\\n\\r]+$)")){
                    filePaths.add(filepath);
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public Object process(ContentNode contentNode, String s, Map<String, Object> map) {
        Xref xref = xrefMap.get(s);
        Document docTarget = xref.getDocument();
        StringBuilder replacement = new StringBuilder("xref:");
        String anchor = xref.getAnchor();

        // If we enter this if-block, then we assume that we are linking OUTSIDE of this doc.
        // For example, from one standlone module to another standalone module.
        // If we DO NOT enter this if-block, then we assume that we are linking INSIDE of this doc.
        // For example, from one module inside an assembly to another module inside the same assembly.
        if (!getModulePaths().contains(docTarget.getPath())) {
            String targetUuid = docTarget
                    .child(documentVariant.getParentLocale().getName(), DocumentLocale.class).get() // TODO - assume same locale for now
                    .variants().get()
                    .variant(documentVariant.getName()).get() // TODO - assume same variant for now
                    .uuid().get();

            replacement.append(targetUuid);
        } else if (anchor == null || anchor.isEmpty()) {
            // If we land here, it means:
            // 1.) We are linking INSIDE of this document
            // 2.) The user did not specify an anchor.
            // How does this happen? Answer: when you're building an assembly, and the user xrefs from
            // one module to another module via filepath only. In this scenario, we link them to the
            // beginning of the module, which is an anchor that is injected by the include processor.
            anchor = "_" + docTarget.uuid().get();
        }

        String label = Optional.of(xref.getLabel()).filter(str -> !str.isEmpty())
                .orElse(docTarget.getName().replaceAll("\\.adoc$", ".html")); // Matches vanilla behavior

        replacement.append("#")
                .append(anchor == null ? "" : anchor)
                .append("[")
                .append(label)
                .append("]");

        return replacement.toString();
    }

    private Set<String> getModulePaths() {
        return Optional.ofNullable(modulePaths).orElseGet(() -> {
            modulePaths = new HashSet<>();
            toc.getEntries().stream()
                    .map(TableOfContents.Entry::getModule)
                    .map(Module::getPath)
                    .forEach(modulePaths::add);
            return modulePaths;
        });
    }
}
