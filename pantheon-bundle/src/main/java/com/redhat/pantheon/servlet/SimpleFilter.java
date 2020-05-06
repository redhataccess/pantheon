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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletFilterScope;
import org.osgi.service.component.annotations.Component;
//import org.apache.felix.scr.annotations.Component;
//import org.apache.felix.scr.annotations.Properties;
//import org.apache.felix.scr.annotations.Property;
//import org.apache.felix.scr.annotations.Service;
//import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Simple Filter
 * 
 * Annotations below are short version of:
 * 
 * @Component
 * @Service(Filter.class)
 * @Properties({
 *     @Property(name="service.description", value="A Simple Filter"),
 *     @Property(name="service.vendor", value="The Apache Software Foundation"),
 *     @Property(name="sling.filter.scope", value="REQUEST"),
 *     @Property(name="service.ranking", intValue=1)
 * })
 */
@Component
@SlingServletFilter(scope = {SlingServletFilterScope.REQUEST},
                    methods = {"GET","HEAD"})
public class SimpleFilter implements Filter {
    
    public static final String DOMAIN_ALLOWED = ".redhat.com";
    private final Logger log = LoggerFactory.getLogger(SimpleFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException,
            ServletException {
        final SlingHttpServletRequest request = (SlingHttpServletRequest)req;
        final SlingHttpServletResponse response = (SlingHttpServletResponse)res;
        String origin = request.getHeader("Origin");
        if (origin != null) {
            if (origin.contains(DOMAIN_ALLOWED)) {
                 response.addHeader("Access-control-Allow-Origin", origin);
                 response.addHeader("Access-control-Allow-Methods", "GET, HEAD, OPTIONS");
            }
        }
        chain.doFilter(request, response);
    }
    public void destroy() {
    }

}
