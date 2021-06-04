package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.validation.helper.XrefValidationHelper;
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
import java.util.Map;

import static com.redhat.pantheon.helper.PantheonConstants.LATEST_SUFFIX;
import static com.redhat.pantheon.helper.PantheonConstants.RELEASED_SUFFIXES;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsBoolean;

/**
 * Renders an HTML preview for a single document variant.
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
        resourceTypes = { "pantheon/moduleVariant", "pantheon/assemblyVariant" },
        methods = "GET",
        extensions = "preview")
public class DocumentVariantRenderServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(DocumentVariantRenderServlet.class);

    private AsciidoctorService asciidoctorService;

    @Activate
    public DocumentVariantRenderServlet(
            @Reference AsciidoctorService asciidoctorService) {
        this.asciidoctorService = asciidoctorService;
    }

    /**
     * Exists only to be overridden by test class.
     * @param request
     * @return
     */
    protected String getSuffix(SlingHttpServletRequest request) {
        return request.getRequestPathInfo().getSuffix();
    }

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException, IOException {
        String suffix = getSuffix(request);
        boolean latest = false;
        if (LATEST_SUFFIX.equals(suffix)) {
            latest = true;
        } else if (!RELEASED_SUFFIXES.contains(suffix)) {
            throw new ServletException("Unrecognized suffix: " + suffix + ". Valid values are '/latest', '/released', and unspecified.");
        }

        boolean reRender = paramValueAsBoolean(request, PantheonConstants.PARAM_RERENDER);

        DocumentVariant variant = request.getResource().adaptTo(DocumentVariant.class);

        if(!latest && variant.released().get() == null) { // This is presumably safe, at least one version should exist
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Released content not found for "
                    + variant.getName()
                    +  " module variant at "
                    + request.getResource().getPath());
            return;
        }
        // collect a list of parameter that traverseFrom with 'ctx_' as those will be used as asciidoctorj
        // parameters
        Map<String, Object> context = asciidoctorService.buildContextFromRequest(request);

        // only allow forced rerendering if this is a draft version. Released and historical revs are written in stone.
        boolean draft = latest && variant.hasDraft();
        XrefValidationHelper.getInstance().initList();
        String html = asciidoctorService.getDocumentHtml(
                variant.getParentLocale().getParent(),
                LocaleUtils.toLocale(variant.getParentLocale().getName()),
                variant.getName(),
                draft,
                context,
                reRender && draft);

        response.setContentType("text/html");
        Writer w = response.getWriter();
        w.write(html);
    }
}

