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
import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.jruby.RubyInstanceConfig;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;


@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="pantheon/modules",
        methods= "GET",
        extensions="preview")
@SuppressWarnings("serial")
public class AsciidocRenderingServlet extends SlingSafeMethodsServlet {
    
    private final Logger log = LoggerFactory.getLogger(AsciidocRenderingServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException,
            IOException {
        Resource resource = request.getResource();
        String html = "NO CONTENT";

        // Get the existing content
        Resource cachedContentNode = resource.getChild("pant:cachedContent");

        // If the content doesn't exist yet, generate and save it
        if( cachedContentNode == null || generatedContentHashDoesntMatch(resource)) {

            html = generateHtml(request, resource);

            request.getResourceResolver().commit();
        } else {
            html = cachedContentNode.getValueMap().get("jcr:data", String.class);
        }

        response.setContentType("text/html");
        Writer w = response.getWriter();

        w.write(html);
    }

    private boolean generatedContentHashDoesntMatch(Resource resource) {
        String srcContent = resource.getValueMap().get("jcr:content/jcr:data", String.class);
        String existingHash = resource.getValueMap().get("pant:cachedContent/pant:hash", String.class);

        return !hash(srcContent).toString().equals(existingHash);
    }

    private String generateHtml(SlingHttpServletRequest request, Resource resource) throws PersistenceException {
        String html;
        String content = resource.getChild("jcr:content").getValueMap().get("jcr:data", String.class);

        RubyInstanceConfig config = new RubyInstanceConfig();
        config.setLoader(Thread.currentThread().getContextClassLoader());

        Asciidoctor instance = Asciidoctor.Factory.create(
                singletonList("uri:classloader:/gems/asciidoctor-1.5.8/lib"));

        // Register any extensions
        instance.javaExtensionRegistry().includeProcessor(
               new SlingResourceIncludeProcessor(request.getResourceResolver(), resource));

        // build the attributes (default + those coming from http parameters)
        AttributesBuilder atts = AttributesBuilder.attributes()
                // show the title on the generated html
                .attribute("showtitle")
                // link the css instead of embedding it
                .linkCss(true)
                // stylesheet reference
                .styleSheetName("/content/static/asciidoctor-default.css");

        // collect a list of parameter that start with 'ctx_' as those will be used as asciidoctorj
        // parameters
        request.getRequestParameterList().stream().filter(
                p -> p.getName().toLowerCase().startsWith("ctx_")
        )
        .collect(toList())
        .forEach(p -> atts.attribute(p.getName().replaceFirst("ctx_", ""), p.getString()));

        // generate html
        html = instance.convert(
                content,
                OptionsBuilder.options()
                        // we're generating html
                        .backend("html")
                        // no physical file is being generated
                        .toFile(false)
                        // allow for some extra flexibility
                        .safe(SafeMode.UNSAFE) // This probably needs to change
                        .inPlace(false)
                        // Generate the html header and footer
                        .headerFooter(true)
                        .attributes(atts)
                        .get());
        instance.shutdown();

        Map<String, Object> props = new HashMap<>();
        props.put("jcr:data", html);

        if(resource.getChild("pant:cachedContent") != null) {
            request.getResourceResolver().delete(resource.getChild("pant:cachedContent"));
        }
        Resource cachedHtmlResource = request.getResourceResolver().create(resource, "pant:cachedContent", props);

        // this has to be done to modify sling resources
        cachedHtmlResource.adaptTo(ModifiableValueMap.class).put("pant:hash", hash(content).toString());

        return html;
    }

    /*
     * calculates a hash for a string
     * TODO This should probably be moved elsewhere
     */
    private HashCode hash(String str) {
        return Hashing.adler32().hashString(str, Charsets.UTF_8);
    }
}

