package com.redhat.pantheon.servlet.util;

import com.redhat.pantheon.model.module.Module;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.jcr.RepositoryException;

import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Lisa Davidson
 */
@ExtendWith({SlingContextExtension.class})
public class ServletHelperTest {
    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void getResourceByUuidTest() throws RepositoryException {
        // Given
        slingContext.build()
                .resource("/content/repositories/repo/module",
                        "jcr:primaryType", "pant:module")
                .commit();

        registerMockAdapter(Module.class, slingContext);
        ServletHelper servletHelper = new ServletHelper();
        MockSlingHttpServletRequest request = slingContext.request();
        String resourceUuid = slingContext.resourceResolver()
                .getResource("/content/repositories/repo/module")
                .getValueMap()
                .get("jcr:uuid")
                .toString();
        // When
        Resource resource = servletHelper.getResourceByUuid(request, resourceUuid);
        // Then
        assertEquals("pantheon/module", resource.getResourceType());
        assertEquals("pant:module", resource.getValueMap().get("jcr:primaryType"));
    }
}
