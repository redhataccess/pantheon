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

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.redhat.pantheon.conf.AsciidoctorPoolService;
import com.redhat.pantheon.conf.LocalFileManagementService;
import com.redhat.pantheon.model.Module;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;

import static java.util.stream.Collectors.toList;


@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which transforms asciidoc content into html",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletResourceTypes(
        resourceTypes="pantheon/modules",
        methods= "GET",
        extensions="preview")
@SuppressWarnings("serial")
public class AsciidocRenderingServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(AsciidocRenderingServlet.class);

    private LocalFileManagementService localFileManagementService;
    private AsciidoctorPoolService asciidoctorPoolService;
    private ServiceResourceResolverProvider serviceResourceResolverProvider;

    @Activate
    public AsciidocRenderingServlet(
            @Reference LocalFileManagementService localFileManagementService,
            @Reference AsciidoctorPoolService asciidoctorPoolService,
            @Reference ServiceResourceResolverProvider serviceResourceResolverProvider) {
        this.localFileManagementService = localFileManagementService;
        this.asciidoctorPoolService = asciidoctorPoolService;
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
    }

    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException,
            IOException {
        Resource resource = request.getResource();
        String html = "NO CONTENT";
        final Module module = resource.adaptTo(Module.class);
        String cachedContent = module.getCachedHtmlContent();

        // If the content doesn't exist yet, generate and save it
        if( cachedContent == null || !generatedContentHashMatches(resource) || request.getParameter("rerender") != null) {
            Content c = generateHtml(request, resource);
            cacheContent(request, module, c);
            html = c.html;
        } else {
            html = module.getCachedHtmlContent();
        }

        response.setContentType("text/html");
        Writer w = response.getWriter();

        w.write(html);
    }

    private boolean generatedContentHashMatches(Resource resource) {
        String srcContent = resource.getValueMap().get("asciidoc/jcr:content/jcr:data", String.class);
        String existingHash = resource.getValueMap().get("cachedContent/pant:hash", String.class);

        boolean match = hash(srcContent).toString().equals(existingHash);

        return match;
    }

    private Content generateHtml(SlingHttpServletRequest request, Resource resource) throws PersistenceException, IOException {
        Content c = new Content();
        // TODO remove the use of Resource (if possible)
        Module module = resource.adaptTo(Module.class);
        c.asciidoc = module.getAsciidocContent();

        // build the attributes (default + those coming from http parameters)
        AttributesBuilder atts = AttributesBuilder.attributes()
                // show the title on the generated html
                .attribute("showtitle")
                // link the css instead of embedding it
                .linkCss(true)
                // stylesheet reference
                .styleSheetName("/static/rhdocs.css");

        // collect a list of parameter that start with 'ctx_' as those will be used as asciidoctorj
        // parameters
        request.getRequestParameterList().stream().filter(
                p -> p.getName().toLowerCase().startsWith("ctx_")
        )
        .collect(toList())
        .forEach(p -> atts.attribute(p.getName().replaceFirst("ctx_", ""), p.getString()));

        // generate html
        OptionsBuilder ob = OptionsBuilder.options()
                // we're generating html
                .backend("html")
                // no physical file is being generated
                .toFile(false)
                // allow for some extra flexibility
                .safe(SafeMode.UNSAFE) // This probably needs to change
                .inPlace(false)
                // Generate the html header and footer
                .headerFooter(true)
                .attributes(atts);
        localFileManagementService.getTemplateDirectory().ifPresent(ob::templateDir);

        long start = System.currentTimeMillis();
        Asciidoctor asciidoctor = asciidoctorPoolService.requestInstance(resource);
        try {
            c.html = asciidoctor.convert(
                    c.asciidoc,
                    ob.get());
        } finally {
            asciidoctorPoolService.releaseInstance(asciidoctor);
        }
        log.info("Rendering finished in {} ms.", System.currentTimeMillis() - start);

        return c;
    }

    private void cacheContent(final SlingHttpServletRequest request, final Module readOnlyModule, final Content content) {
        try {
            ResourceResolver serviceResourceResolver = serviceResourceResolverProvider.getServiceResourceResolver();
            // reload from the service
            Module module = serviceResourceResolver.getResource(readOnlyModule.getPath()).adaptTo(Module.class);

            module.getCachedContent()
                .setHash(hash(content.asciidoc).toString());
            module.getCachedContent()
                .setData(content.html);
            serviceResourceResolver.commit();
            serviceResourceResolver.close();
        } catch (Exception e) {
            e.printStackTrace(); // FIXME
            throw new RuntimeException(e);
        }
    }

    /*
     * calculates a hash for a string
     * TODO This should probably be moved elsewhere
     */
    private HashCode hash(String str) {
        return Hashing.adler32().hashString(str == null ? "" : str, Charsets.UTF_8);
    }

    private class Content {
        public String html;
        public String asciidoc;
    }
}

