package com.redhat.pantheon.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.pantheon.model.api.SlingModels;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class ProductTest {

    private final SlingContext slingContext = new SlingContext();

    @Test
    void createNewProduct() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/product_tests/product1")
                .commit();
        
        // When
        Product product = SlingModels.getModel(
                slingContext.resourceResolver().getResource("/content/product_tests/product1"),
                Product.class);

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/content/product_tests/product1"));
        assertNotNull(product.name());
    }

    @Test
    void createNewVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/product_tests/product1/versions/1")
                .commit();
        
        // When
        ProductVersion productVersion  = SlingModels.getModel(
                slingContext.resourceResolver().getResource("/content/product_tests/product1/versions/1"),
                ProductVersion.class
        );

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/content/product_tests/product1/versions/1"));
        assertNotNull(productVersion.name());
    }
}