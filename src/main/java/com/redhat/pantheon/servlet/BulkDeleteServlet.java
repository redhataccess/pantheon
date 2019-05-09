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
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceNotFoundException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
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
        methods= "POST")
@SuppressWarnings("serial")
public class BulkDeleteServlet extends SlingAllMethodsServlet {

	private final Logger logger = LoggerFactory.getLogger(BulkDeleteServlet.class);
	
	private static final String CONTENT_PATH_PREFIX = "/content/";
	
    public BulkDeleteServlet() {
    }
    
	@Override
    protected void doPost(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        String[] checkboxValues = request.getParameterValues("module");
        List<String> resourcePaths = Arrays.asList(checkboxValues);
        ResourceResolver resourceResolver = request.getResourceResolver();
        try {
        	
        	for ( String rPath: resourcePaths) {
        		Resource res = resourceResolver.getResource(CONTENT_PATH_PREFIX + rPath);
        		// Delete the resource.
        		if (res != null) {
        			resourceResolver.delete(res);
        		} else {
        			String msg = "Missing Resource " + CONTENT_PATH_PREFIX + rPath + " for delete";
        			response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
        			throw new ResourceNotFoundException(msg);
        		}
        	}
        } 
        catch (Exception e) {
        	// Log the error.
        	logger.error("Module deletion failed: {}", e.getMessage(), e);
        } finally {
        	try {
        		resourceResolver.commit();
        	} catch (PersistenceException e) {
        		// Log the error.
        		logger.error("Module delete commit failed {}", e.getMessage(), e);
        		// Revert all pending changes.
        		if (resourceResolver.hasChanges()) {
        			resourceResolver.revert();
        			String commitError = "Something unexpected happened.";
        			response.sendError(HttpServletResponse.SC_BAD_REQUEST, commitError);
        		}
        	}
        	response.sendRedirect("/modules.html");
        	
        	// When done, close the ResourceResolver.
        	resourceResolver.close();
        }   
    }
}