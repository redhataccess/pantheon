package com.redhat.pantheon.data;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.apache.sling.testing.resourceresolver.MockResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.query.Query;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class ModuleDataRetrieverTest {

    @Mock
    private ResourceResolver resourceResolver;


    @Test
    @DisplayName("Search for available modules and get empty results")
    public void testSearchAvailableModulesEmptyResult() throws Exception {
        // Given
        ModuleDataRetriever retriever = new ModuleDataRetriever(resourceResolver);

        // When
        // Normally this is instantiated thru sly in the html
        lenient().when(resourceResolver.findResources(anyString(), eq(Query.JCR_SQL2)))
                .thenReturn(Collections.emptyIterator());
        List<Map<String, Object>> createSortResults = retriever.getModulesCreateSort("any search term");
        List<Map<String, Object>> nameSortResults = retriever.getModulesNameSort("any search term");

        // Then
        //We Expect an empty list because we have not added any modules.
        assertTrue(createSortResults.isEmpty());
        assertTrue(nameSortResults.isEmpty());
    }

    @Test
    @DisplayName("Search for available modules sorted by creation date")
    public void testSearchModulesCreateSort() throws Exception {
        // Given
        final List<Resource> resources = newArrayList(
                new MockResource("/content/repos/test/module1", newHashMap(), resourceResolver),
                new MockResource("/content/repos/test/module2", newHashMap(), resourceResolver),
                new MockResource("/content/repos/test/module3", newHashMap(), resourceResolver)
        );
        ModuleDataRetriever retriever = new ModuleDataRetriever(resourceResolver);

        // When
        lenient().when(resourceResolver.findResources(anyString(), eq(Query.JCR_SQL2)))
                .thenReturn(resources.iterator());
        List<Map<String, Object>> results = retriever.getModulesCreateSort("any search term");

        // Then
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Search for available modules sorted by name")
    public void testSearchModulesNameSort() throws Exception {
        // Given
        final List<Resource> resources = newArrayList(
                new MockResource("/content/repos/test/module1", newHashMap(), resourceResolver),
                new MockResource("/content/repos/test/module2", newHashMap(), resourceResolver),
                new MockResource("/content/repos/test/module3", newHashMap(), resourceResolver)
        );
        ModuleDataRetriever retriever = new ModuleDataRetriever(resourceResolver);

        // When
        lenient().when(resourceResolver.findResources(anyString(), eq(Query.JCR_SQL2)))
                .thenReturn(resources.iterator());
        List<Map<String, Object>> results = retriever.getModulesNameSort("any search term");

        // Then
        assertEquals(3, results.size());
    }
}
