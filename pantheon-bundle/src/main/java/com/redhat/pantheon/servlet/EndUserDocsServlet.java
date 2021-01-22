package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorPool;
import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import com.redhat.pantheon.model.assembly.TableOfContents;
import com.redhat.pantheon.model.document.Document;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Optional;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which allows on-demand rendering of Pantheon help documentation",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(
        value = "/pantheon/staticdocs"
)
public class EndUserDocsServlet extends SlingSafeMethodsServlet {

    private AsciidoctorPool asciidoctorPool;
    private AsciidoctorService asciidoctorService;

    @Activate
    public EndUserDocsServlet(@Reference AsciidoctorPool asciidoctorPool, @Reference AsciidoctorService asciidoctorService) {
        this.asciidoctorPool = asciidoctorPool;
        this.asciidoctorService = asciidoctorService;
    }

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        Optional<String> staticdoc = Optional.ofNullable(request.getRequestParameter("document")).map(RequestParameter::getString);
        String attributeDoc = (String) request.getAttribute("document");

        String document = staticdoc.orElse(attributeDoc);

        Resource resource = request.getResourceResolver().getResource("/content/docs/Pantheon/entities/pantheon-bundle/src/main/resources/SLING-INF/content/docs/" + document);

        String html = "";
        StringBuilder content = new StringBuilder();
        if (resource != null && !staticdoc.isPresent()) {
            Document doc = resource.adaptTo(Document.class);
            html = asciidoctorService.getDocumentHtml(doc, DEFAULT_MODULE_LOCALE, doc.getWorkspace().getCanonicalVariantName(), true, null, true);
        } else {
            resource = request.getResourceResolver().getResource("/content/staticdocs/" + document);
            content.append(resource.getChild("jcr:content").getValueMap().get("jcr:data", String.class));

            OptionsBuilder ob = OptionsBuilder.options()
                    // we're generating html
                    .backend("html")
                    // no physical file is being generated
                    .toFile(false)
                    // allow for some extra flexibility
                    .safe(SafeMode.UNSAFE) // This probably needs to change
                    .inPlace(false)
                    // Generate the html header and footer
                    .headerFooter(true);

            Asciidoctor asciidoctor = asciidoctorPool.borrowObject();
            try {
                asciidoctor.javaExtensionRegistry().includeProcessor(
                        new SlingResourceIncludeProcessor(resource, new TableOfContents(), null));

                html = asciidoctor.convert(content.toString(), ob.get());
            } finally {
                asciidoctorPool.returnObject(asciidoctor);
            }
        }
        response.setContentType("text/html");
        response.getWriter().write(html);
    }
}
