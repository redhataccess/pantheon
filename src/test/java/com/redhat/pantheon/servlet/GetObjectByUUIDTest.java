package com.redhat.pantheon.servlet;

import org.apache.http.HttpStatus;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({MockitoExtension.class, SlingContextExtension.class})
class GetObjectByUUIDTest {

    SlingContext sCtx = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void doGet() throws ServletException, IOException {
        // Given
        sCtx.create().resource("/test",
                "name", "a-name",
                "jcr:mixinTypes", "mix:referenceable");
        sCtx.create().resource("/test/child",
                "name", "child-name");
        String resourceUuid = sCtx.resourceResolver()
                .getResource("/test")
                .getValueMap()
                .get("jcr:uuid")
                .toString();
        Map params = newHashMap();
        params.put("uuid", resourceUuid);
        params.put("depth",  "2");
        sCtx.request().setParameterMap(params);
        GetObjectByUUID servlet = new GetObjectByUUID();

        // When
        servlet.doGet(sCtx.request(), sCtx.response());

        // Then
        assertEquals(HttpStatus.SC_OK, sCtx.response().getStatus());
    }

    @Test
    void doGetNonexistentResource() throws ServletException, IOException {
        // Given
        Map params = newHashMap();
        params.put("uuid", UUID.randomUUID().toString());
        sCtx.request().setParameterMap(params);
        GetObjectByUUID servlet = new GetObjectByUUID();

        // When
        servlet.doGet(sCtx.request(), sCtx.response());

        // Then
        assertEquals(HttpStatus.SC_NOT_FOUND, sCtx.response().getStatus());
    }

    @Test
    void doGetWithNoUUID() throws ServletException, IOException {
        // Given
        GetObjectByUUID servlet = new GetObjectByUUID();

        // When
        servlet.doGet(sCtx.request(), sCtx.response());

        // Then
        assertEquals(HttpStatus.SC_BAD_REQUEST, sCtx.response().getStatus());
    }

    @Test
    void doGetWithInvalidDepth() throws ServletException, IOException {
        // Given
        Map params = newHashMap();
        params.put("uuid", UUID.randomUUID().toString());
        params.put("depth", "-1");
        sCtx.request().setParameterMap(params);
        GetObjectByUUID servlet = new GetObjectByUUID();

        // When
        servlet.doGet(sCtx.request(), sCtx.response());

        // Then
        assertEquals(HttpStatus.SC_BAD_REQUEST, sCtx.response().getStatus());
    }

    @Test
    void doGetWithSpecificDereference() throws ServletException, IOException {
        // Given
        sCtx.create().resource("/test",
                "name", "a-name",
                "jcr:mixinTypes", "mix:referenceable");
        String resourceUuid = sCtx.resourceResolver()
                .getResource("/test")
                .getValueMap()
                .get("jcr:uuid")
                .toString();
        sCtx.create().resource("/test2",
                "name", "child-name",
                "referenceField", resourceUuid,
                "sling:resourceType", "pantheon/test",
                "jcr:mixinTypes", "mix:referenceable");
        String resource2Uuid = sCtx.resourceResolver()
                .getResource("/test2")
                .getValueMap()
                .get("jcr:uuid")
                .toString();

        Map params = newHashMap();
        params.put("uuid", resource2Uuid);
        params.put("depth",  "2");
        params.put("dereference", "dummyType:dummyField,pantheon/test:referenceField");
        sCtx.request().setParameterMap(params);
        GetObjectByUUID servlet = new GetObjectByUUID();

        // When
        servlet.doGet(sCtx.request(), sCtx.response());

        // Then
        assertEquals(HttpStatus.SC_OK, sCtx.response().getStatus());
        assertEquals(true, sCtx.response().getOutputAsString().contains("a-name"));
    }

    @Test
    void doGetWithGlobalDereference() throws ServletException, IOException {
        // Given
        sCtx.create().resource("/test",
                "name", "a-name",
                "jcr:mixinTypes", "mix:referenceable");
        String resourceUuid = sCtx.resourceResolver()
                .getResource("/test")
                .getValueMap()
                .get("jcr:uuid")
                .toString();
        sCtx.create().resource("/test2",
                "name", "child-name",
                "referenceField", resourceUuid,
                "sling:resourceType", "pantheon/test",
                "jcr:mixinTypes", "mix:referenceable");
        String resource2Uuid = sCtx.resourceResolver()
                .getResource("/test2")
                .getValueMap()
                .get("jcr:uuid")
                .toString();

        Map params = newHashMap();
        params.put("uuid", resource2Uuid);
        params.put("depth",  "2");
        params.put("dereference", "dummyField,referenceField");
        sCtx.request().setParameterMap(params);
        GetObjectByUUID servlet = new GetObjectByUUID();

        // When
        servlet.doGet(sCtx.request(), sCtx.response());

        // Then
        assertEquals(HttpStatus.SC_OK, sCtx.response().getStatus());
        assertEquals(true, sCtx.response().getOutputAsString().contains("a-name"));
    }
}