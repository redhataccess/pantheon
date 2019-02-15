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

import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.jruby.RubyInstanceConfig;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Hello World Servlet registered by resource type
 *
 * Annotations below are short version of:
 * 
 * @Component
 * @Service(Servlet.class)
 * @Properties({
 *    @Property(name="service.description", value="Hello World Type Servlet"),
 *    @Property(name="service.vendor", value="The Apache Software Foundation"),
 *    @Property(name="sling.servlet.resourceTypes", value="sling/servlet/default"),
 *    @Property(name="sling.servlet.selectors", value="hello"),
 *    @Property(name="sling.servlet.extensions", value="html")
 * })
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=pantheon/modules",
                "sling.servlet.extensions=preview",
                Constants.SERVICE_DESCRIPTION+"=Servlet which transforms asciidoc content into html",
                Constants.SERVICE_VENDOR+"=Red Hat Content Tooling team"
        })
@SuppressWarnings("serial")
public class AsciidocRenderingServlet extends SlingSafeMethodsServlet {
    
    private final Logger log = LoggerFactory.getLogger(AsciidocRenderingServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException,
            IOException {
        Resource resource = request.getResource();
        String html = "<NO CONTENT>";

        // Get the existing content
        Resource cachedContentNode = resource.getChild("pant:cachedContent");

        // If the content doesn't exist yet, generate and save it
        if( cachedContentNode == null ) {

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

            Map<String, Object> props = new HashMap<>();
            props.put("jcr:data", html);

            request.getResourceResolver().create(resource, "pant:cachedContent", props);
            request.getResourceResolver().commit();
            instance.shutdown();
        } else {
            html = cachedContentNode.getValueMap().get("jcr:data", String.class);
        }

        response.setContentType("text/html");
        Writer w = response.getWriter();

        w.write(html);
    }

}

