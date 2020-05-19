package com.redhat.pantheon.model;

import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.Module;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class ModuleTest {

    private final SlingContext slingContext = new SlingContext();

    @Test
    void createNewVersion() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1")
                .commit();
        slingContext.registerAdapter(Resource.class, Node.class, mock(Node.class));
        Module module =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/module1"),
                        Module.class);

        // When
        module.createModuleLocale(new Locale("es", "ES"))
                .source()
                .getOrCreate()
                .draft()
                .getOrCreate();
        module.getResourceResolver().commit();

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/content/module1/es_ES/source/draft"));
    }

    @Test
    void getVersionByLocaleAndVersionName() throws Exception {
        // Given
        slingContext.build()
                // module 1, en_US
                .resource("/content/module1/en_US/source/released")
                .resource("/content/module1/en_US/source/draft")
                // module 1, es_ES
                .resource("/content/module1/es_ES/source/released")
                .resource("/content/module1/es_ES/source/draft")
                // module 2, en_US
                .resource("/content/module2/en_US/source/released")
                .resource("/content/module2/en_US/source/draft")
                // module 2, es_ES
                .resource("/content/module2/es_ES/source/released")
                .resource("/content/module2/es_ES/source/draft")
                .commit();
        slingContext.registerAdapter(Resource.class, Node.class, mock(Node.class));

        // When
        Module module1 =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/module1"), Module.class);
        Module module2 =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/module2"), Module.class);

        // Then
        assertNotNull(
                module1.moduleLocale(new Locale("en", "US")).get().source().getOrCreate().child("released", FileResource.class).get());
        assertNotNull(
                module1.moduleLocale(new Locale("en", "US")).get().source().getOrCreate().child("draft", FileResource.class).get());
        assertNotNull(
                module1.moduleLocale(new Locale("es", "ES")).get().source().getOrCreate().child("released", FileResource.class).get());
        assertNotNull(
                module1.moduleLocale(new Locale("es", "ES")).get().source().getOrCreate().child("draft", FileResource.class).get());
        assertNotNull(
                module2.moduleLocale(new Locale("en", "US")).get().source().getOrCreate().child("released", FileResource.class).get());
        assertNotNull(
                module2.moduleLocale(new Locale("en", "US")).get().source().getOrCreate().child("draft", FileResource.class).get());
        assertNotNull(
                module2.moduleLocale(new Locale("es", "ES")).get().source().getOrCreate().child("released", FileResource.class).get());
        assertNotNull(
                module2.moduleLocale(new Locale("es", "ES")).get().source().getOrCreate().child("draft", FileResource.class).get());

        assertNull(
                module1.moduleLocale(Locale.SIMPLIFIED_CHINESE));
        assertFalse(
                module1.moduleLocale(new Locale("es", "ES")).get().source().getOrCreate().child("abc", FileResource.class).isPresent());
    }

}