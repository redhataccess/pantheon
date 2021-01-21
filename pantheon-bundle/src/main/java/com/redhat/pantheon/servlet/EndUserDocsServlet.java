package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorPool;
import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import com.redhat.pantheon.model.assembly.TableOfContents;
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

    @Activate
    public EndUserDocsServlet(@Reference AsciidoctorPool asciidoctorPool) {
        this.asciidoctorPool = asciidoctorPool;
    }

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        String document = Optional.ofNullable(request.getRequestParameter("document")).map(RequestParameter::getString)
                .orElse((String) request.getAttribute("document"));

        Resource resource = request.getResourceResolver().getResource("/content/docs/" + document);

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
        String html = "";
        try {
            asciidoctor.javaExtensionRegistry().includeProcessor(
                    new SlingResourceIncludeProcessor(resource, new TableOfContents(), null));

            StringBuilder content = new StringBuilder(resource.getChild("jcr:content").getValueMap().get("jcr:data", String.class));

            html = asciidoctor.convert(content.toString(), ob.get());
        } finally {
            asciidoctorPool.returnObject(asciidoctor);
        }
        response.setContentType("text/html");
        response.getWriter().write(html);
    }
}
