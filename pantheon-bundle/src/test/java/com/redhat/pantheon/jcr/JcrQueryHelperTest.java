package com.redhat.pantheon.jcr;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class JcrQueryHelperTest {

    // Testing with a real JCR repository
    private final SlingContext slingContext = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    @DisplayName("Test queryAll simple call")
    public void testQueryAll() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module")
        .commit();
        JcrQueryHelper helper = new JcrQueryHelper(slingContext.resourceResolver());

        // When
        Stream<Resource> results = helper.queryAll("pant:module");

        // Then
        assertEquals(1, results.count());
    }

    @Test
    @DisplayName("Test queryAll with limit")
    public void testQueryAllWithLimit() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module2",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module3",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module4",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module5",
                        "jcr:primaryType", "pant:module")
                .commit();
        JcrQueryHelper helper = new JcrQueryHelper(slingContext.resourceResolver());

        // When
        Stream<Resource> results = helper.queryAll("pant:module", 3, 0);

        // Then
        List<Resource> resultList = results.collect(Collectors.toList());
        assertEquals(3, resultList.size());
        assertEquals("/content/module1", resultList.get(0).getPath());
        assertEquals("/content/module2", resultList.get(1).getPath());
        assertEquals("/content/module3", resultList.get(2).getPath());
    }

    @Test
    @DisplayName("Test queryAll with an offset")
    public void testQueryAllWithOffset() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module2",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module3",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module4",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module5",
                        "jcr:primaryType", "pant:module")
                .commit();
        JcrQueryHelper helper = new JcrQueryHelper(slingContext.resourceResolver());

        // When
        Stream<Resource> results = helper.queryAll("pant:module", Long.MAX_VALUE, 2);

        // Then
        List<Resource> resultList = results.collect(Collectors.toList());
        assertEquals(3, resultList.size());
        assertEquals("/content/module3", resultList.get(0).getPath());
        assertEquals("/content/module4", resultList.get(1).getPath());
        assertEquals("/content/module5", resultList.get(2).getPath());
    }

    @Test
    @DisplayName("Test queryAll with an offset and a limit")
    public void testQueryAllWithOffsetAndLimits() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module2",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module3",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module4",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module5",
                        "jcr:primaryType", "pant:module")
                .commit();
        JcrQueryHelper helper = new JcrQueryHelper(slingContext.resourceResolver());

        // When
        Stream<Resource> results = helper.queryAll("pant:module", 2, 2);

        // Then
        List<Resource> resultList = results.collect(Collectors.toList());
        assertEquals(2, resultList.size());
        assertEquals("/content/module3", resultList.get(0).getPath());
        assertEquals("/content/module4", resultList.get(1).getPath());
    }

    @Test
    @DisplayName("Test query")
    public void testQuery() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module2",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module3",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module4",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module5",
                        "jcr:primaryType", "pant:module")
                .commit();
        JcrQueryHelper helper = new JcrQueryHelper(slingContext.resourceResolver());

        // When
        Stream<Resource> results = helper.query("select * from [pant:module]");

        // Then
        List<Resource> resultList = results.collect(Collectors.toList());
        assertEquals(5, resultList.size());
    }

    @Test
    @DisplayName("Test query with limits and offset")
    public void testQueryWithLimitsAndOffset() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module2",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module3",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module4",
                        "jcr:primaryType", "pant:module")
                .resource("/content/module5",
                        "jcr:primaryType", "pant:module")
                .commit();
        JcrQueryHelper helper = new JcrQueryHelper(slingContext.resourceResolver());

        // When
        Stream<Resource> results = helper.query("select * from [pant:module] as m order by [jcr:path]", 2, 2);

        // Then
        List<Resource> resultList = results.collect(Collectors.toList());
        assertEquals(2, resultList.size());
        assertEquals("/content/module3", resultList.get(0).getPath());
        assertEquals("/content/module4", resultList.get(1).getPath());
    }
}
