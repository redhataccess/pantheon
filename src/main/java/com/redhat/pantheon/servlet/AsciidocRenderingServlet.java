/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.redhat.pantheon.servlet;

import com.google.common.base.Strings;
import com.redhat.pantheon.asciidoctor.AsciidoctorService;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.Module;
import com.redhat.pantheon.model.ModuleRevision;
import com.redhat.pantheon.util.function.FunctionalUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
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

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.redhat.pantheon.conf.GlobalConfig.DEFAULT_MODULE_LOCALE;
import static com.redhat.pantheon.servlet.ServletUtils.paramValue;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsBoolean;
import static com.redhat.pantheon.util.function.FunctionalUtils.nullSafe;
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

    private AsciidoctorService asciidoctorService;

    @Activate
    public AsciidocRenderingServlet(
            @Reference AsciidoctorService asciidoctorService) {
        this.asciidoctorService = asciidoctorService;
    }

    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException, IOException {
        String locale = paramValue(request, "locale", DEFAULT_MODULE_LOCALE.toString());
        String revision = paramValue(request, "rev");
        log.info("Locale set to: " + locale);

        Module module = request.getResource().adaptTo(Module.class);
        Locale localeObj = LocaleUtils.toLocale(locale);
        ModuleRevision moduleRevision = module.findRevision(localeObj, revision);

        if(moduleRevision == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Revision " + revision + " not found for" +
                    " module at " + request.getResource().getPath());
        }
        else {
            // collect a list of parameter that start with 'ctx_' as those will be used as asciidoctorj
            // parameters
            Map<String, Object> context = request.getRequestParameterList().stream().filter(
                    p -> p.getName().toLowerCase().startsWith("ctx_")
            )
            .collect(toMap(
                    reqParam -> reqParam.getName().replaceFirst("ctx_", ""),
                    reqParam -> reqParam.getString())
            );

            String html = asciidoctorService.getModuleHtml(
                    module, localeObj, revision, context, paramValueAsBoolean(request, PARAM_RERENDER));

            response.setContentType("text/html");
            Writer w = response.getWriter();
            w.write(html);
        }
    }
}

