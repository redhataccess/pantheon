package com.redhat.pantheon.model;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
                .createNextVersion();
        module.getResourceResolver().commit();

        // Then
        assertNotNull(slingContext.resourceResolver().getResource("/content/module1/es_ES/1"));
    }

    @Test
    void getVersionByLocaleAndVersionName() throws Exception {
        // Given
        slingContext.build()
                // module 1, en_US
                .resource("/content/module1/en_US/1")
                .resource("/content/module1/en_US/2")
                // module 1, es_ES
                .resource("/content/module1/es_ES/1")
                .resource("/content/module1/es_ES/2")
                // module 2, en_US
                .resource("/content/module2/en_US/1")
                .resource("/content/module2/en_US/2")
                // module 2, es_ES
                .resource("/content/module2/es_ES/1")
                .resource("/content/module2/es_ES/2")
                .commit();
        slingContext.registerAdapter(Resource.class, Node.class, mock(Node.class));

        // When
        Module module1 =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/module1"), Module.class);
        Module module2 =
                SlingModels.getModel(slingContext.resourceResolver().getResource("/content/module2"), Module.class);

        // Then
        assertNotNull(
                module1.getModuleLocale(new Locale("en", "US")).getVersion("1"));
        assertNotNull(
                module1.getModuleLocale(new Locale("en", "US")).getVersion("2"));
        assertNotNull(
                module1.getModuleLocale(new Locale("es", "ES")).getVersion("1"));
        assertNotNull(
                module1.getModuleLocale(new Locale("es", "ES")).getVersion("2"));
        assertNotNull(
                module2.getModuleLocale(new Locale("en", "US")).getVersion("1"));
        assertNotNull(
                module2.getModuleLocale(new Locale("en", "US")).getVersion("2"));
        assertNotNull(
                module2.getModuleLocale(new Locale("es", "ES")).getVersion("1"));
        assertNotNull(
                module2.getModuleLocale(new Locale("es", "ES")).getVersion("2"));

        assertNull(
                module1.getModuleLocale(Locale.SIMPLIFIED_CHINESE));
        assertNull(
                module1.getModuleLocale(new Locale("es", "ES")).getVersion("abc"));
    }

}