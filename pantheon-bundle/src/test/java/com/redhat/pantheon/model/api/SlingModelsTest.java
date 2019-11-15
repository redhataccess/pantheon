package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.api.v2.Child;
import com.redhat.pantheon.model.api.v2.Field;
import com.redhat.pantheon.model.api.v2.Reference;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Named;
import java.util.Calendar;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SlingContextExtension.class})
class SlingModelsTest {

    SlingContext sc;

    @Test
    void simpleModel() {
        // Given
        sc.build()
                .resource("/test",
                        "name", "my name")
                .resource("/test/child")
                .resource("/test/child/grandchild",
                        "jcr:number", 10)
                .commit();

        // When
        TestResource model = SlingModels.getModel(sc.resourceResolver(), "/test", TestResource.class);

        // Then
        assertEquals("my name", model.name().get());
        model.name().set("new name");
        assertEquals("new name", model.name().get());
        assertEquals("/test", model.getPath());
        assertTrue(ChildResource.class.isAssignableFrom(model.child().get().getClass()));
        assertNotNull(model.child().get().grandchild());
        assertTrue(10L == model.child().get().grandchild().get().number().get());
    }

    @Test
    void setters() {
        // Given
        sc.build()
                .resource("/test")
                .commit();
        Calendar now = Calendar.getInstance();
        String[] arrayValue = {"A", "B", "C"};

        // When
        TestResource model = SlingModels.getModel(sc.resourceResolver(), "/test", TestResource.class);
        model.name().set("new name");
        model.dateField().set(now);
        model.booleanField().set(true);
        model.intField().set(10);
        model.longField().set(20L);
        model.stringArrayField().set(arrayValue);
        model.enumField().set(TestResource.Value.VALUE_2);

        // Then
        assertEquals("new name", model.name().get());
        assertEquals(now, model.dateField().get());
        assertEquals(true, model.booleanField().get());
        assertEquals(new Integer(10), model.intField().get());
        assertEquals(new Long(20), model.longField().get());
        assertEquals(arrayValue, model.stringArrayField().get());
        assertEquals(TestResource.Value.VALUE_2, model.enumField().get());
    }

    @Test
    void getters() {
        // Given
        Calendar now = Calendar.getInstance();
        String[] arrayValue = {"A", "B", "C"};
        sc.build()
                .resource("/test",
                        "name", "new name",
                        "longField", 20L,
                        "intField", 10,
                        "booleanField", true,
                        "dateField", now,
                        "stringArrayField", arrayValue,
                        "enumField", TestResource.Value.VALUE_2)
                .commit();

        // When
        TestResource model = SlingModels.getModel(sc.resourceResolver(), "/test", TestResource.class);

        // Then
        assertEquals("new name", model.name().get());
        assertEquals(now, model.dateField().get());
        assertEquals(true, model.booleanField().get());
        assertEquals(new Integer(10), model.intField().get());
        assertEquals(new Long(20), model.longField().get());
        assertEquals(arrayValue, model.stringArrayField().get());
        assertEquals(TestResource.Value.VALUE_2, model.enumField().get());
    }

    @Test
    void getDeepField() throws Exception {
        // Given
        sc.build()
                .resource("/test/child/grandchild",
                        "jcr:number", 10)
                .commit();

        // When
        TestResource model = SlingModels.getModel(sc.resourceResolver(), "/test", TestResource.class);

        // Then
        assertEquals(new Long(10), model.deepField().get());
    }

    @Test
    void createChild() {
        // Given
        sc.build()
                .resource("/test")
                .commit();

        // When
        TestResource model = SlingModels.getModel(sc.resourceResolver(), "/test", TestResource.class);
        ChildResource child = model.createChild("child", ChildResource.class);
        Grandchild grandchild = child.createChild("grandchild", Grandchild.class);

        // Then
        assertNotNull(child);
        assertNotNull(grandchild);
        assertThrows(RuntimeException.class, () -> model.createChild("child", ChildResource.class),
                "Same child cannot be created twice");
    }

    @Test
    void getChild() {
        // Given
        sc.build()
                .resource("/test/child/grandchild")
                .commit();

        // When
        TestResource model = SlingModels.getModel(sc.resourceResolver(), "/test", TestResource.class);
        ChildResource child = model.getChild("child", ChildResource.class);
        Grandchild grandchild = child.getChild("grandchild", Grandchild.class);

        // Then
        assertNotNull(child);
        assertNotNull(grandchild);
        assertNull(model.getChild("nonexistent"));
    }

    @Test
    void getOrCreateChild() {
        // Given
        sc.build()
                .resource("/test/child")
                .commit();

        // When
        TestResource model = SlingModels.getModel(sc.resourceResolver(), "/test", TestResource.class);

        // Then
        assertNotNull(model.child().get());
        assertEquals(model.child().get().getName(), model.child().getOrCreate().getName());
        assertNull(model.child().get().grandchild().get());
        assertNotNull(model.child().get().grandchild().getOrCreate());
        assertNotNull(model.child().get().grandchild().get());
    }

    @Test
    void create() throws Exception {
        // Given
        ResourceResolver resourceResolver = sc.resourceResolver();
        Resource root = resourceResolver.getResource("/");

        // When
        TestResource model = SlingModels.getModel(resourceResolver.create(root,"test", null), TestResource.class);
        model.name().set("new name");
        model.longField().set(10L);
        resourceResolver.commit();

        // Then
        TestResource storedModel = SlingModels.getModel(resourceResolver, "/test", TestResource.class);
        assertEquals("new name", storedModel.name().get());
        assertEquals(new Long(10), storedModel.longField().get());
    }

    @Test
    void toMap() {
        // Given
        String[] arrayValue = {"A", "B", "C"};
        sc.build()
                .resource("/test",
                        "name", "new name",
                        "longField", 20L,
                        "intField", 10,
                        "booleanField", true,
                        "dateField", Calendar.getInstance(),
                        "stringArrayField", arrayValue,
                        "enumField", TestResource.Value.VALUE_2)
                .commit();

        // When
        Map<String, Object> map = SlingModels.getModel(sc.resourceResolver(), "/test", TestResource.class)
                .getValueMap();

        // Then
        assertEquals("new name", map.get("name"));
        assertEquals(20L, map.get("longField"));
        assertTrue(map.containsKey("dateField"));
        assertTrue(map.containsKey("booleanField"));
        assertEquals(true, map.get("booleanField"));
        assertTrue(map.containsKey("stringArrayField"));
        assertArrayEquals(arrayValue, (String[])map.get("stringArrayField"));
        assertEquals(TestResource.Value.VALUE_2.name(), ((TestResource.Value)map.get("enumField")).name());
    }

    @Test
    void unrecognizedMethod() {
        // Given
        sc.build()
                .resource("/test")
                .commit();

        // When
        WronglyDefinedResource model = SlingModels.getModel(sc.resourceResolver(), "/test", WronglyDefinedResource.class);

        // Then
        assertThrows(IllegalArgumentException.class, () -> model.wrongMethod(true));
    }

    interface TestResource extends SlingModel {

        public enum Value {
            VALUE_1,
            VALUE_2;
        }

        Field<String> name();
        Field<Long> longField();
        Field<Integer> intField();
        Field<Boolean> booleanField();
        Field<Calendar> dateField();
        Field<String[]> stringArrayField();
        Field<Value> enumField();

        @Named("child/grandchild/jcr:number")
        Field<Long> deepField();

        @Named("child")
        Child<ChildResource> child();
    }

    interface ChildResource extends SlingModel {

        Child<Grandchild> grandchild();
    }

    interface Grandchild extends SlingModel {

        @Named("jcr:number")
        Field<Long> number();
    }

    interface WronglyDefinedResource extends SlingModel {

        Long wrongMethod(boolean param);
    }
}
