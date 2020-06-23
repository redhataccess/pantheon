package com.redhat.pantheon.servlet;

import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.ModuleVariant;
import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import com.redhat.pantheon.validation.validators.NotNullValidator;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.servlet.ServletException;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({SlingContextExtension.class})
public class StatusAcknowledgementServletTest {

    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);
    String testHTML = "<!DOCTYPE html> <html lang=\"en\"> <head><title>test title</title></head> <body "
            + "class=\"article\"><h1>test title</h1> </header> </body> </html>";

    @BeforeEach
    public void setUp() {
        slingContext.create()
                .resource("/content/repositories/repo/entities/module/en_US/variants/test",
                        "jcr:primaryType", "pant:moduleVariant");
        slingContext.create()
                .resource("/content/repositories/repo/entities/module/en_US/variants/test/released",
                        "jcr:primaryType", "pant:moduleVersion");
        slingContext.create()
                .resource("/content/repositories/repo/entities/module/en_US/variants/test/released/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description");
        slingContext.create()
                .resource("/content/repositories/repo/entities/module/en_US/variants/test/released/cached_html/jcr:content",
                        "jcr:data", testHTML);
        registerMockAdapter(ModuleVariant.class, slingContext);
    }

    @Test
    public void testAddAcknowledgement() throws ServletException, IOException {

        String resourceUuid = slingContext.resourceResolver()
                .getResource("/content/repositories/repo/entities/module/en_US/variants/test")
                .getValueMap()
                .get("jcr:uuid")
                .toString();

        Charset utf8 = Charset.forName("UTF-8");

        String data = "{\"id\":\"" + resourceUuid + "\",\"variant\": \"test\",\"status\": \"received\",\"sender\":\"hydra\",\"message\":\"from hydra\"}";
        //slingContext.request().setContent(data.getBytes(utf8));
        MockSlingHttpServletRequest mockSlingHttpServletRequest = new MockSlingHttpServletRequest(slingContext.resourceResolver());
        mockSlingHttpServletRequest.setContent(data.getBytes(utf8));
        StatusAcknowledgeServlet statusAcknowledgeServlet = new StatusAcknowledgeServlet();
        statusAcknowledgeServlet.setNotNullValidator(new NotNullValidator());

        statusAcknowledgeServlet.doPost(mockSlingHttpServletRequest, slingContext.response());
        Assertions.assertEquals(200, slingContext.response().getStatus(), "Status should be 200");
        ModuleVariant moduleVariant
                = SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/content/repositories/repo/entities/module/en_US/variants/test"),
                        ModuleVariant.class);
        assertEquals("received", moduleVariant.released()
                .get().ackStatus()
                .get().status().get());
        assertEquals("from hydra", moduleVariant.released().get().ackStatus().get().message().get());
        assertEquals("hydra", moduleVariant.released().get().ackStatus().get().sender().get());
    }

    @Test
    public void testAddAcknowledgementWithoutRequiredFields() throws ServletException, IOException {

        String resourceUuid = slingContext.resourceResolver()
                .getResource("/content/repositories/repo/entities/module/en_US/variants/test")
                .getValueMap()
                .get("jcr:uuid")
                .toString();

        Charset utf8 = Charset.forName("UTF-8");
        String data = "{\"id\":\"" + resourceUuid + "\",\"variant\": \"test\",\"status\": \"received\",\"sender\":\"hydra\"}";
        MockSlingHttpServletRequest mockSlingHttpServletRequest = new MockSlingHttpServletRequest(slingContext.resourceResolver());
        mockSlingHttpServletRequest.setContent(data.getBytes(utf8));
        StatusAcknowledgeServlet statusAcknowledgeServlet = new StatusAcknowledgeServlet();
        statusAcknowledgeServlet.setNotNullValidator(new NotNullValidator());

        statusAcknowledgeServlet.doPost(mockSlingHttpServletRequest, slingContext.response());
        Assertions.assertEquals(400, slingContext.response().getStatus(), "Status should be 400");
    }

    @Test
    public void testAddAcknowledgementWhenDraft() throws ServletException, IOException {

        slingContext.create()
                .resource("/content/repositories/repo/entities/module1/en_US/variants/test",
                        "jcr:primaryType", "pant:moduleVariant");
        slingContext.create()
                .resource("/content/repositories/repo/entities/module1/en_US/variants/test/draft",
                        "jcr:primaryType", "pant:moduleVersion");
        slingContext.create()
                .resource("/content/repositories/repo/entities/module1/en_US/variants/test/draft/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description");
        slingContext.create()
                .resource("/content/repositories/repo/entities/module1/en_US/variants/test/draft/cached_html/jcr:content",
                        "jcr:data", testHTML);

        registerMockAdapter(ModuleVariant.class, slingContext);

        String resourceUuid = slingContext.resourceResolver()
                .getResource("/content/repositories/repo/entities/module1/en_US/variants/test")
                .getValueMap()
                .get("jcr:uuid")
                .toString();

        Charset utf8 = Charset.forName("UTF-8");
        String data = "{\"id\":\"" + resourceUuid + "\",\"variant\": \"test\",\"status\": \"received\",\"sender\":\"hydra\",\"message\":\"from hydra\"}";
        MockSlingHttpServletRequest mockSlingHttpServletRequest = new MockSlingHttpServletRequest(slingContext.resourceResolver());
        mockSlingHttpServletRequest.setContent(data.getBytes(utf8));
        StatusAcknowledgeServlet statusAcknowledgeServlet = new StatusAcknowledgeServlet();
        statusAcknowledgeServlet.setNotNullValidator(new NotNullValidator());

        statusAcknowledgeServlet.doPost(mockSlingHttpServletRequest, slingContext.response());
        Assertions.assertEquals(200, slingContext.response().getStatus(), "Status should be 200");
        ModuleVariant moduleVariant
                = SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/content/repositories/repo/entities/module1/en_US/variants/test"),
                        ModuleVariant.class);
        assertNotNull(moduleVariant.draft().get().ackStatus().get());
        assertEquals("received", moduleVariant.draft()
                .get().ackStatus()
                .get().status().get());
        assertEquals("from hydra", moduleVariant.draft().get().ackStatus().get().message().get());
        assertEquals("hydra", moduleVariant.draft().get().ackStatus().get().sender().get());
    }
}
