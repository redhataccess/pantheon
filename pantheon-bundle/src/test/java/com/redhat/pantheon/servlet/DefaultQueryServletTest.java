package com.redhat.pantheon.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.resourcebuilder.api.ResourceBuilder;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class DefaultQueryServletTest {

    private final SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    private DefaultQueryServlet servlet;

    @BeforeEach
    public void prepareJcrRepo() {
        ResourceBuilder builder = slingContext.build();
        for (int i = 0; i < 10; i++) {
            builder.resource("/content/test/node-" + i,
                    "name", "node-" + i,
                    "number", i);
        }
        builder.commit();
    }

    @BeforeEach
    public void initializeServlet() throws ServletException {
        servlet = new DefaultQueryServlet();
        servlet.init();
    }

    @Test
    @DisplayName("Test a simple query")
    public void testSimpleQuery() throws Exception {
        // Given
        slingContext.request().setResource(slingContext.resourceResolver().getResource("/content/test"));

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        JsonNode jsonNode = new ObjectMapper().readTree(slingContext.response().getOutputAsString());
        assertEquals(10L, jsonNode.get("size").asLong());
        assertEquals(10L, jsonNode.get("nextOffset").asLong());
        assertTrue(jsonNode.get("results").isArray());
    }

    @Test
    @DisplayName("Test a sorted query")
    public void testSortedQuery() throws Exception {
        // Given
        Map<String, Object> paramMap = newHashMap();
        slingContext.request().setResource(slingContext.resourceResolver().getResource("/content/test"));
        paramMap.put("orderBy", new String[]{"name desc"});
        slingContext.request().setParameterMap(paramMap);

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        JsonNode jsonNode = new ObjectMapper().readTree(slingContext.response().getOutputAsString());
        assertEquals(10L, jsonNode.get("size").asLong());
        assertEquals(10L, jsonNode.get("nextOffset").asLong());
        assertTrue(jsonNode.get("results").isArray());
        Iterator<JsonNode> resultIterator = jsonNode.get("results").iterator();
        // verify the order is inversed
        for (int i = 0; i < 10; i++) {
            assertEquals("node-" + (9-i), resultIterator.next().get("name").asText());
        }
    }

    @Test
    @DisplayName("Test a filtered query")
    public void testFilteredQuery() throws Exception {
        // Given
        Map<String, Object> paramMap = newHashMap();
        slingContext.request().setResource(slingContext.resourceResolver().getResource("/content/test"));
        paramMap.put("where", new String[]{"number < 5"});
        slingContext.request().setParameterMap(paramMap);

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        JsonNode jsonNode = new ObjectMapper().readTree(slingContext.response().getOutputAsString());
        assertEquals(5L, jsonNode.get("size").asLong());
        assertEquals(5L, jsonNode.get("nextOffset").asLong());
        assertTrue(jsonNode.get("results").isArray());
        Iterator<JsonNode> resultIterator = jsonNode.get("results").iterator();
        // verify the right nodes are included
        resultIterator.forEachRemaining(n -> assertTrue(n.get("number").asLong() < 5));
    }

    @Test
    @DisplayName("Test paged Query")
    public void testPagedQuery() throws Exception {
        // Given
        Map<String, Object> paramMap = newHashMap();
        paramMap.put("limit", new String[]{"5"});
        slingContext.request().setParameterMap(paramMap);
        slingContext.request().setResource(slingContext.resourceResolver().getResource("/content/test"));

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        JsonNode jsonNode = new ObjectMapper().readTree(slingContext.response().getOutputAsString());
        assertEquals(5L, jsonNode.get("size").asLong());
        assertEquals(5L, jsonNode.get("nextOffset").asLong());
        assertTrue(jsonNode.get("results").isArray());
    }

    @Test
    @DisplayName("Test paged Query followup")
    public void testPagedQueryFollowup() throws Exception {
        // Given
        Map<String, Object> paramMap = newHashMap();
        slingContext.request().setResource(slingContext.resourceResolver().getResource("/content/test"));
        paramMap.put("limit", new String[]{"5"});
        paramMap.put("offset", new String[]{"5"});
        slingContext.request().setParameterMap(paramMap);


        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        JsonNode jsonNode = new ObjectMapper().readTree(slingContext.response().getOutputAsString());
        assertEquals(5L, jsonNode.get("size").asLong());
        assertEquals(10L, jsonNode.get("nextOffset").asLong());
        assertTrue(jsonNode.get("results").isArray());
    }

    @Test
    @DisplayName("Test custom node type")
    public void testCustomNodeType() throws Exception {
        // Given
        ResourceBuilder builder = slingContext.build();
        builder.resource("/content/test/customNode",
                "name", "customNode",
                "jcr:primaryType", "pant:module")
            .commit();

        Map<String, Object> paramMap = newHashMap();
        slingContext.request().setResource(slingContext.resourceResolver().getResource("/content/test"));
        paramMap.put("nodeType", new String[]{"pant:module"});
        slingContext.request().setParameterMap(paramMap);

        // When
        servlet.doGet(slingContext.request(), slingContext.response());

        // Then
        JsonNode jsonNode = new ObjectMapper().readTree(slingContext.response().getOutputAsString());
        assertEquals(1L, jsonNode.get("size").asLong());
        assertEquals(1L, jsonNode.get("nextOffset").asLong());
        assertEquals("pant:module", jsonNode.get("results").iterator().next().get("jcr:primaryType").asText());
    }
}
