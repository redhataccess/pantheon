package com.redhat.pantheon.servlet.assembly;

import com.google.common.base.Charsets;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.html.Html;
import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVersion;
import com.redhat.pantheon.servlet.ServletUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

/**
 * @author Carlos Munoz
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Renders an assembly page in html",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/api/assembly")
public class AssemblyPageRenderingServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        String assemblyId = ServletUtils.paramValue(request, "id");
        Long pageNum = ServletUtils.paramValueAsLong(request, "page", 1L);

        try {
            // First get the assembly
            // Since there are no assemblies yet, we will get modules
            JcrQueryHelper helper = new JcrQueryHelper(request.getResourceResolver());
            Resource assemblyResource = helper.findById(assemblyId);
            Module assembly = SlingModels.getModel(assemblyResource, Module.class);

            // Get the page content
            // Split by sections with class 'sect1' as they seem to be the top level sections
            // Assuming latest draft or released version, and only in English for now
            Optional<ModuleVersion> versionOpt = assembly.getReleasedVersion(GlobalConfig.DEFAULT_MODULE_LOCALE);
            ModuleVersion version = versionOpt.orElse( assembly.getDraftVersion(GlobalConfig.DEFAULT_MODULE_LOCALE).get() );
            String fullHtmlContent = version.content().get().cachedHtml().get().data().get();
            // Now split it into the right page
            String pageHtml = Html.parse(Charsets.UTF_8.name())
                    .andThen(Document::body)
                    .andThen(element -> element.select("section.sect1"))
                    .andThen(elements -> elements.listIterator(pageNum.intValue()-1).next())
                    .andThen(element -> element.outerHtml())
                    .apply(fullHtmlContent);

            // Render the page as html
            response.setContentType("text/html");
            Writer w = response.getWriter();
            w.write(pageHtml);
        } catch (RepositoryException e) {
            throw new ServletException(e);
        }
    }
}
