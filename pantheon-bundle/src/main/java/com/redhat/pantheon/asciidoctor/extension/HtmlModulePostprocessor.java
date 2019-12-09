package com.redhat.pantheon.asciidoctor.extension;

import com.google.common.base.Charsets;
import com.redhat.pantheon.html.Html;
import org.apache.sling.api.resource.Resource;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;

/**
 * An asciidoctor extension to perform all final html modifications for a Module's html.
 *
 * @see com.redhat.pantheon.servlet.assets.ImageServletFilter for how some of the changes done by this processor are
 * handled
 * @author Carlos Munoz
 */
public class HtmlModulePostprocessor extends Postprocessor {

    private final Resource module;

    public HtmlModulePostprocessor(Resource module) {
        this.module = module;
    }

    @Override
    public String process(Document document, String output) {
        return Html.parse(Charsets.UTF_8.name())
                .andThen(Html.encodeAllImageLocations(module))
                .andThen(Html.encodeAllXrefs())
                .apply(output)
                .toString();
    }
}
