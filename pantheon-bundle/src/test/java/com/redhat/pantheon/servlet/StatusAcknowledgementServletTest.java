package com.redhat.pantheon.servlet;

import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.validation.validators.NotNullValidator;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.redhat.pantheon.util.TestUtils.registerMockAdapter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({SlingContextExtension.class})
public class StatusAcknowledgementServletTest {
    SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);
    String testHTML = "<!DOCTYPE html> <html lang=\"en\"> <head><title>test title</title></head> <body " +
            "class=\"article\"><h1>test title</h1> </header> </body> </html>";

    @BeforeEach
    public void setUp(){
        slingContext.create()
                .resource("/content/repositories/repo/module",
                        "jcr:primaryType", "pant:module");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US",
                        "jcr:primaryType", "pant:moduleLocale");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/1",
                        "jcr:primaryType", "pant:moduleVersion");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/1/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description");
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/1/content/asciidoc",
                        "jcr:content", testHTML);
        slingContext.create()
                .resource("/content/repositories/repo/module/en_US/1/content/cachedHtml",
                        "jcr:data", testHTML);

        slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US").adaptTo(ModifiableValueMap.class)
                .put("released", slingContext.resourceResolver().getResource("/content/repositories/repo/module/en_US/1").getValueMap()
                        .get("jcr:uuid"));
        registerMockAdapter(Module.class, slingContext);
    }
    @Test
    public void testAddAcknowledgement() throws ServletException, IOException {

        String resourceUuid = slingContext.resourceResolver()
                .getResource("/content/repositories/repo/module")
                .getValueMap()
                .get("jcr:uuid")
                .toString();

        Charset utf8 = Charset.forName("UTF-8");

        String data = "{\"id\":\""+resourceUuid+"\",\"status\": \"received\",\"sender\":\"hydra\",\"message\":\"from hydra\"}";
        //slingContext.request().setContent(data.getBytes(utf8));
        MockSlingHttpServletRequest mockSlingHttpServletRequest = new MockSlingHttpServletRequest(slingContext.resourceResolver());
        mockSlingHttpServletRequest.setContent(data.getBytes(utf8));
        StatusAcknowledgeServlet statusAcknowledgeServlet = new StatusAcknowledgeServlet();
        statusAcknowledgeServlet.setNotNullValidator(new NotNullValidator());

        statusAcknowledgeServlet.doPost(mockSlingHttpServletRequest, slingContext.response());
        Assertions.assertEquals(200, slingContext.response().getStatus(), "Status should be 200");
        Module module =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/content/repositories/repo/module"),
                        Module.class);
        assertNotNull(module.getReleasedVersion(LocaleUtils.toLocale("en_US")).get().ackStatus().get());
        assertEquals("received", module.getReleasedVersion(LocaleUtils.toLocale("en_US"))
                .get().ackStatus()
                .get().status().get());
        assertEquals("from hydra", module.getReleasedVersion(LocaleUtils.toLocale("en_US")).get().ackStatus().get().message().get());
        assertEquals("hydra", module.getReleasedVersion(LocaleUtils.toLocale("en_US")).get().ackStatus().get().sender().get());
    }

    @Test
    public void testAddAcknowledgementWithoutRequiredFields() throws ServletException, IOException {

        String resourceUuid = slingContext.resourceResolver()
                .getResource("/content/repositories/repo/module")
                .getValueMap()
                .get("jcr:uuid")
                .toString();

        Charset utf8 = Charset.forName("UTF-8");
        String data = "{\"id\":\"" + resourceUuid + "\",\"status\": \"received\",\"sender\":\"hydra\"}";
        MockSlingHttpServletRequest mockSlingHttpServletRequest = new MockSlingHttpServletRequest(slingContext.resourceResolver());
        mockSlingHttpServletRequest.setContent(data.getBytes(utf8));
        StatusAcknowledgeServlet statusAcknowledgeServlet = new StatusAcknowledgeServlet();
        statusAcknowledgeServlet.setNotNullValidator(new NotNullValidator());

        statusAcknowledgeServlet.doPost(mockSlingHttpServletRequest, slingContext.response());
        Assertions.assertEquals( 400, slingContext.response().getStatus(), "Status should be 400");
    }

    @Test
    public void testAddAcknowledgementWhenTheLocaleIsNotSupported() throws ServletException, IOException {

        slingContext.create()
                .resource("/content/repositories/repo/module1",
                        "jcr:primaryType", "pant:module");
        slingContext.create()
                .resource("/content/repositories/repo/module1/es_ES",
                        "jcr:primaryType", "pant:moduleLocale");
        slingContext.create()
                .resource("/content/repositories/repo/module1/es_ES/1",
                        "jcr:primaryType", "pant:moduleVersion");
        slingContext.create()
                .resource("/content/repositories/repo/module1/es_ES/1/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description");
        slingContext.create()
                .resource("/content/repositories/repo/module1/es_ES/1/content/asciidoc",
                        "jcr:content", testHTML);
        slingContext.create()
                .resource("/content/repositories/repo/module1/es_ES/1/content/cachedHtml",
                        "jcr:data", testHTML);

        slingContext.resourceResolver().getResource("/content/repositories/repo/module1/es_ES").adaptTo(ModifiableValueMap.class)
                .put("released", slingContext.resourceResolver().getResource("/content/repositories/repo/module1/es_ES/1").getValueMap()
                        .get("jcr:uuid"));
        registerMockAdapter(Module.class, slingContext);
        String resourceUuid = slingContext.resourceResolver()
                .getResource("/content/repositories/repo/module1")
                .getValueMap()
                .get("jcr:uuid")
                .toString();

        Charset utf8 = Charset.forName("UTF-8");
        String data = "{\"id\":\""+resourceUuid+"\",\"status\": \"received\",\"sender\":\"hydra\",\"message\":\"from hydra\"}";
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
                .resource("/content/repositories/repo/module1",
                        "jcr:primaryType", "pant:module");
        slingContext.create()
                .resource("/content/repositories/repo/module1/en_US",
                        "jcr:primaryType", "pant:moduleLocale");
        slingContext.create()
                .resource("/content/repositories/repo/module1/en_US/1",
                        "jcr:primaryType", "pant:moduleVersion");
        slingContext.create()
                .resource("/content/repositories/repo/module1/en_US/1/metadata",
                        "jcr:title", "A title",
                        "jcr:description", "A description");
        slingContext.create()
                .resource("/content/repositories/repo/module1/en_US/1/content/asciidoc",
                        "jcr:content", testHTML);
        slingContext.create()
                .resource("/content/repositories/repo/module1/en_US/1/content/cachedHtml",
                        "jcr:data", testHTML);

        slingContext.resourceResolver().getResource("/content/repositories/repo/module1/en_US").adaptTo(ModifiableValueMap.class)
                .put("draft", slingContext.resourceResolver().getResource("/content/repositories/repo/module1/en_US/1").getValueMap()
                        .get("jcr:uuid"));
        registerMockAdapter(Module.class, slingContext);

        String resourceUuid = slingContext.resourceResolver()
                .getResource("/content/repositories/repo/module1")
                .getValueMap()
                .get("jcr:uuid")
                .toString();

        Charset utf8 = Charset.forName("UTF-8");
        String data = "{\"id\":\""+resourceUuid+"\",\"status\": \"received\",\"sender\":\"hydra\",\"message\":\"from hydra\"}";
        MockSlingHttpServletRequest mockSlingHttpServletRequest = new MockSlingHttpServletRequest(slingContext.resourceResolver());
        mockSlingHttpServletRequest.setContent(data.getBytes(utf8));
        StatusAcknowledgeServlet statusAcknowledgeServlet = new StatusAcknowledgeServlet();
        statusAcknowledgeServlet.setNotNullValidator(new NotNullValidator());

        statusAcknowledgeServlet.doPost(mockSlingHttpServletRequest, slingContext.response());
        Assertions.assertEquals(200, slingContext.response().getStatus(), "Status should be 200");
        Module module =
                SlingModels.getModel(
                        slingContext.resourceResolver().getResource("/content/repositories/repo/module1"),
                        Module.class);
        assertNotNull(module.getDraftVersion(LocaleUtils.toLocale("en_US")).get().ackStatus().get());
        assertEquals("received", module.getDraftVersion(LocaleUtils.toLocale("en_US"))
                .get().ackStatus()
                .get().status().get());
        assertEquals("from hydra", module.getDraftVersion(LocaleUtils.toLocale("en_US")).get().ackStatus().get().message().get());
        assertEquals("hydra", module.getDraftVersion(LocaleUtils.toLocale("en_US")).get().ackStatus().get().sender().get());
    }
}
