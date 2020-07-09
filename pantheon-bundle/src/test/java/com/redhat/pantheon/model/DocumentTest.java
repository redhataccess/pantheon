package com.redhat.pantheon.model;

import com.redhat.pantheon.model.document.Document;
import com.redhat.pantheon.model.api.SlingModels;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class DocumentTest {
    private final SlingContext slingContext = new SlingContext();

    @Test
    void createNewVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/document1")
                .commit();
        slingContext.registerAdapter(Resource.class, Node.class, mock(Node.class));
        Document document =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/document1"),
                        Document.class);

        // When
        document.getLocale(new Locale("es", "ES")).create()
                .getSource()
                .getOrCreate()
                .draft()
                .getOrCreate();
        document.getResourceResolver().commit();

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/content/document1/es_ES/source/draft"));
    }
}
