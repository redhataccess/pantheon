package com.redhat.pantheon.servlet;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;

import javax.servlet.FilterChain;

import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.testing.mock.sling.MockSling;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({SlingContextExtension.class})
public class CorsEnablingFilterTest {

    private SlingHttpServletResponse response = null;
    private FilterChain chain = null;
    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @BeforeEach
    public void setUp() throws Exception {
        // Create mocks for required variables
        response = mock(SlingHttpServletResponse.class);
        chain = mock(FilterChain.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Clear variables during teardown
        // Not actually necessary as they get re-initialized in setUp
        response = null;
        chain = null;
    }

    @Test
    public void testDoFilter() throws Exception {
        // prepare sling request
        ResourceResolver resourceResolver = MockSling.newResourceResolver(slingContext.bundleContext());
        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resourceResolver);

        // simulate query string
        request.setQueryString("locale=en-us&module_id=123-456-789");

        // set current resource
        request.setResource(resourceResolver.getResource("/api/module"));

        // set method
        request.setMethod(HttpConstants.METHOD_GET);

        // set headers
        request.addHeader("Origin", "https://www.redhat.com");

        CorsEnablingFilter filter = new CorsEnablingFilter();

        // Execute the method with the mocks we want to test
        filter.doFilter(request, response, chain);

        // Verify that chain.doFilter() was called
        verify(chain).doFilter(request, response);
        verify(response).addHeader("Access-control-Allow-Origin", "https://www.redhat.com");
        verify(response).addHeader("Access-control-Allow-Methods", "GET, HEAD, OPTIONS");
    }
}