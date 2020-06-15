package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.model.Rendering;
import com.redhat.pantheon.model.module.HashableFileResource;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleLocale;
import com.redhat.pantheon.model.module.SourceContent;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.model.module.ModuleVariant.DEFAULT_VARIANT_NAME;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsBoolean;

/**
 * Renders an HTML preview for a single module.
 * To provide parameters to the asciidoc generation process, provide the parameters with their name prefixed
 * with "ctx_".
 *
 * For example, if an asciidoc attribute of name 'product' needs to be passed, there will need to be a
 * query parameter of name 'ctx_product' provided in the url.
 */
@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which transforms asciidoc content into html",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletResourceTypes(
        resourceTypes = { "pantheon/module" },
        methods = "GET",
        extensions = "preview")
@SuppressWarnings("serial")
public class RenderingServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(RenderingServlet.class);

    private AsciidoctorService asciidoctorService;

    @Activate
    public RenderingServlet(
            @Reference AsciidoctorService asciidoctorService) {
        this.asciidoctorService = asciidoctorService;
    }

    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException, IOException {
        if(request.getResource().getResourceType().equals("pantheon/module")){
            new ModuleRendering(asciidoctorService).getRenderedHTML(request,response);
        }
        else if(request.getResource().getResourceType().equals("pantheon/assembly")){
            new AssemblyRendering(asciidoctorService).getRenderedHTML(request,response);
        }
    }
}

