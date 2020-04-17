package com.redhat.pantheon.servlet;

import org.apache.sling.api.resource.ResourceResolver;

import java.security.Principal;

public class MockSlingHttpServletRequest extends org.apache.sling.servlethelpers.MockSlingHttpServletRequest{
    public MockSlingHttpServletRequest(ResourceResolver resourceResolver) {
        super(resourceResolver);
    }

    @Override
    public Principal getUserPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return "demo";
            }
        };
    }

}
