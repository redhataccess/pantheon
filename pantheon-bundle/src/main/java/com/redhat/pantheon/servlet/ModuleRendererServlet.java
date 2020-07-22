package com.redhat.pantheon.servlet;

import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.document.SourceContent;
import com.redhat.pantheon.model.module.*;
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
import java.lang.reflect.TypeVariable;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.model.module.ModuleVariant.DEFAULT_VARIANT_NAME;
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
        resourceTypes = { "pantheon/module"},
        methods = "GET",
        extensions = "preview")
public class ModuleRendererServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(ModuleRendererServlet.class);

    private AsciidoctorService asciidoctorService;

    @Activate
    public ModuleRendererServlet(
            @Reference AsciidoctorService asciidoctorService) {
        this.asciidoctorService = asciidoctorService;
    }

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException, IOException {
        String locale = paramValue(request, PantheonConstants.PARAM_LOCALE, DEFAULT_MODULE_LOCALE.toString());
        boolean draft = paramValueAsBoolean(request, PantheonConstants.PARAM_DRAFT);
        boolean reRender = paramValueAsBoolean(request, PantheonConstants.PARAM_RERENDER);
        String variantName = paramValue(request, PantheonConstants.PARAM_VARIANT, DEFAULT_VARIANT_NAME);
        Locale localeObj = LocaleUtils.toLocale(locale);

        Module module = request.getResource().adaptTo(Module.class);
        System.out.println("Module 1!!!");
        tellMeAbout(module);
        Module module2 = SlingModels.getModel(request.getResourceResolver().getResource(module.getPath()), Module.class);
        tellMeAbout(module2);

        Optional<HashableFileResource> moduleVariantSource = module.locale(localeObj)
                .traverse()
                .toChild(ModuleLocale::source)
                .toChild(draft ? SourceContent::draft : SourceContent::released)
                .getAsOptional();

        if(!moduleVariantSource.isPresent()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, (draft ? "Draft " : "Released ")
                    + "source content not found for " + variantName +  " module variant at "
                    + request.getResource().getPath());
            return;
        }
        // collect a list of parameter that traverseFrom with 'ctx_' as those will be used as asciidoctorj
        // parameters
        Map<String, Object> context = asciidoctorService.buildContextFromRequest(request);

        // only allow forced rerendering if this is a draft version. Released and historical revs are written in stone.
        String html = asciidoctorService.getModuleHtml(
                module, localeObj, variantName, draft, context, reRender && draft);

        response.setContentType("text/html");
        Writer w = response.getWriter();
        w.write(html);
    }

    private static void tellMeAbout(Object o) {
        System.out.println("\nTelling you about!!");
        System.out.println("o: " + o);
        System.out.println("o class: " + o.getClass());
        System.out.println("o type: " + o.getClass().getTypeName());
        for (TypeVariable<? extends Class<?>> i : o.getClass().getTypeParameters()) {
            System.out.println("Type parameter! " + i);
        }

        System.out.println("Class tree:");
        Class c = o.getClass();
        while (c != null) {
            System.out.println(c);
            c = c.getSuperclass();
        }
        System.out.println("done");
        System.out.println(o.getClass().getCanonicalName());
        for (Class<?> i : o.getClass().getInterfaces()) {
            System.out.println("Interface: " + i);
        }
        System.out.println("DONE TALKING!");
    }
}

