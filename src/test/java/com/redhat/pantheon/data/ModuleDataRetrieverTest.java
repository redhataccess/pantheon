package com.redhat.pantheon.data;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.jcr.MockJcr;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class ModuleDataRetrieverTest {

    // Running with a full JCR implementation
    private final SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);
    private ResourceResolver resourceResolver;

    @BeforeEach
    public void prepareTest() {
        // Need to spy the object as mock sling doesn't seem to be properly registering adapter functions
        resourceResolver = spy(slingContext.resourceResolver());
    }

    @AfterEach
    public void cleanUpTest() {
    }

    @Test
    @DisplayName("Search for available modules and get empty results")
    public void testSearchAvailableModulesEmptyResult() throws Exception {
        // Given
        ModuleDataRetriever retriever = new ModuleDataRetriever(resourceResolver);

        // When
        when(resourceResolver.adaptTo(Session.class)).thenReturn(MockJcr.newSession());
        // Normally this is instantiated thru sly in the html
        List<Map<String, Object>> createSortResults = retriever.getModulesCreateSort("any search term");
        List<Map<String, Object>> nameSortResults = retriever.getModulesNameSort("any search term");

        // Then
        //We Expect an empty list because we have not added any modules.
        assertTrue(createSortResults.isEmpty());
        assertTrue(nameSortResults.isEmpty());
    }

    @Test
    @DisplayName("Search for modules with a null search term")
    public void testSearchModulesNullArgs() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/modules/test/module1",
                        "sling:resourceType", "pantheon/module")
                .resource("/content/modules/test/module2",
                        "sling:resourceType", "pantheon/module")
                .resource("/content/modules/test/module3",
                        "sling:resourceType", "pantheon/module")
                .commit();
        ModuleDataRetriever retriever = new ModuleDataRetriever(resourceResolver);

        // When
        List<Map<String, Object>> results = retriever.getModulesSort(null, null, null, 0, 10);

        // Then
        // should return all modules
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Search for available modules sorted by creation date")
    public void testSearchModulesCreateSort() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/modules/test/module1",
                        "sling:resourceType", "pantheon/module")
                .resource("/content/modules/test/module2",
                        "sling:resourceType", "pantheon/module")
                .resource("/content/modules/test/module3",
                        "sling:resourceType", "pantheon/module")
                .commit();
        ModuleDataRetriever retriever = new ModuleDataRetriever(resourceResolver);

        // When
        List<Map<String, Object>> results = retriever.getModulesCreateSort("");

        // Then
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Search for available modules sorted by name")
    public void testSearchModulesNameSort() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/modules/test/module1",
                        "sling:resourceType", "pantheon/module")
                .resource("/content/modules/test/module2",
                        "sling:resourceType", "pantheon/module")
                .resource("/content/modules/test/module3",
                        "sling:resourceType", "pantheon/module")
                .commit();
        ModuleDataRetriever retriever = new ModuleDataRetriever(resourceResolver);

        // When
        List<Map<String, Object>> results = retriever.getModulesNameSort("");

        // Then
        assertEquals(3, results.size());
    }
}
