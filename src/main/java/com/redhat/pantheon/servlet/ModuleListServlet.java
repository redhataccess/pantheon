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

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which handles the bulk deletion on module listing page",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        }
        
)
@SlingServletResourceTypes(
        resourceTypes="pantheon/modules",
        methods= "POST",
        extensions="html")
@SuppressWarnings("serial")
public class ModuleListServlet extends SlingAllMethodsServlet {
	private final Logger logger = LoggerFactory.getLogger(ModuleListServlet.class);

    public ModuleListServlet() {
    }
    
    @Override
    protected void doPost(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        final Map<String, Object> base = new LinkedHashMap<>();
        final ValueMapDecorator parameters = new ValueMapDecorator(base);
        final Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            parameters.put(name, request.getRequestParameter(name));
        }
        logger.debug("parameters: {}", parameters);

       
        final String resourcePath = request.getRequestPathInfo().getResourcePath();
        logger.debug("resourcePath is '{}'", resourcePath);
        
            
        Resource r = request.getResourceResolver().getResource("pantheon/modules");
        logger.debug("myResource is '{}'", r);
        //request.getResourceResolver().delete(r);
        //request.getResourceResolver().commit();
       
    }    
}