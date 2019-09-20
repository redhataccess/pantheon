package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.model.module.ModuleVersion;
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
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsBoolean;
import static java.util.stream.Collectors.toMap;

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
public class AsciidocRenderingServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(AsciidocRenderingServlet.class);

    static final String PARAM_RERENDER = "rerender";
    static final String PARAM_DRAFT = "draft";
    static final String PARAM_LOCALE = "locale";

    private AsciidoctorService asciidoctorService;

    @Activate
    public AsciidocRenderingServlet(
            @Reference AsciidoctorService asciidoctorService) {
        this.asciidoctorService = asciidoctorService;
    }

    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException, IOException {
        String locale = paramValue(request, PARAM_LOCALE, DEFAULT_MODULE_LOCALE.toString());
        boolean draft = paramValueAsBoolean(request, PARAM_DRAFT);

        Module module = request.getResource().adaptTo(Module.class);
        Locale localeObj = LocaleUtils.toLocale(locale);

        Optional<ModuleVersion> moduleVersion;

        if(draft) {
            moduleVersion = module.getDraftVersion(localeObj);
        } else {
            moduleVersion = module.getReleasedVersion(localeObj);
        }


        if(!moduleVersion.isPresent()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, (draft ? "Draft " : "Released ")
                    + "content version not found for module at " + request.getResource().getPath());
        }
        else {
            // collect a list of parameter that start with 'ctx_' as those will be used as asciidoctorj
            // parameters
            Map<String, Object> context = asciidoctorService.buildContextFromRequest(request);

            // only allow forced rerendering if this is a draft version. Released and historical revs are written in stone.
            String html = asciidoctorService.getModuleHtml(
                    moduleVersion.get(), module, context, draft && paramValueAsBoolean(request, PARAM_RERENDER));

            response.setContentType("text/html");
            Writer w = response.getWriter();
            w.write(html);
        }
    }
}

