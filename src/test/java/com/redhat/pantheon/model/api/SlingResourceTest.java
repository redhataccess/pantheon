package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class SlingResourceTest {

    private final SlingContext slingContext = new SlingContext();

    private final String[] stringArrayValue = {"one", "two", "three"};

    @Test
    public void fieldMapping() throws Exception {
        slingContext.build()
                .resource("/content/module1",
                        "jcr:name", "my-name",
                        "jcr:date", Calendar.getInstance(),
                        "jcr:number", "10",
                        "jcr:boolean", false,
                        "jcr:stringArray", stringArrayValue)
                .commit();

        TestResource model = new TestResource(slingContext.resourceResolver().getResource("/content/module1"));

        // test conversions to the specific field-declared types
        assertEquals("my-name", model.NAME.get());
        assertEquals(new Long(10), model.NUMBER.get());
        assertEquals(false, model.BOOLEAN.get());
        assertNotNull(model.DATE.get());
        assertEquals(stringArrayValue, model.STRINGARRAY.get());
    }

    @Test
    public void childMapping() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1")
                .resource("child")
                .resource("grandchild")
                .commit();

        // When
        TestResource model = new TestResource(slingContext.resourceResolver().getResource("/content/module1"));

        // Then
        model.CHILD
                .get()
                .GRANDCHILD
                .get();
    }

    @Test
    public void getDeepField() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1")
                .resource("child")
                .resource("grandchild",
                        "jcr:name", "grandchild-name")
                .commit();

        // When
        TestResource model = new TestResource(slingContext.resourceResolver().getResource("/content/module1"));

        // Then
        assertEquals("grandchild-name", model.GRANDCHILD_NAME.get());
    }

    @Test
    public void fieldEditing() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module",
                        "jcr:created", Calendar.getInstance(),
                        "jcr:createdBy", "auser",
                        "jcr:boolean", false,
                        "jcr:stringArray", new String[]{})
                .commit();

        // When
        TestResource model = new TestResource(slingContext.resourceResolver().getResource("/content/module1"));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.YEAR, 2019);
        model.DATE.set(cal);
        model.NAME.set("someoneelse");
        model.NUMBER.set(15L);
        model.BOOLEAN.set(true);
        model.STRINGARRAY.set(stringArrayValue);

        // Then
        assertEquals(1, model.DATE.get().get(Calendar.MONTH));
        assertEquals(1, model.DATE.get().get(Calendar.DATE));
        assertEquals(2019, model.DATE.get().get(Calendar.YEAR));
        assertEquals("someoneelse", model.NAME.get());
        assertEquals(new Long(15), model.NUMBER.get());
        assertEquals(true, model.BOOLEAN.get());
        assertEquals(stringArrayValue, model.STRINGARRAY.get());
    }

    @Test
    public void testGetChild() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module",
                        "jcr:created", Calendar.getInstance(),
                        "jcr:createdBy", "auser")
                .resource("child",
                        "jcr:property", "prop-value")
                .commit();

        // When
        TestResource model = new TestResource(slingContext.resourceResolver().getResource("/content/module1"));

        // Then
        assertNotNull(model.CHILD.get());
        assertEquals("prop-value", model.CHILD.get().getResource().getValueMap().get("jcr:property"));
    }

    @Test
    public void testIsPresent() {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module",
                        "jcr:created", Calendar.getInstance(),
                        "jcr:createdBy", "auser")
                .resource("child",
                        "jcr:property", "prop-value")
                .resource("/content/module2",
                        "jcr:primaryType", "pant:module",
                        "jcr:created", Calendar.getInstance(),
                        "jcr:createdBy", "auser")
                .commit();

        // When
        TestResource model1 = new TestResource(slingContext.resourceResolver().getResource("/content/module1"));
        TestResource model2 = new TestResource(slingContext.resourceResolver().getResource("/content/module2"));

        // Then
        assertTrue(model1.CHILD.isPresent());
        assertFalse(model2.CHILD.isPresent());
        assertNull(model2.CHILD.get());
    }

    @Test
    public void testGetOrCreateChildExists() {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module",
                        "jcr:created", Calendar.getInstance(),
                        "jcr:createdBy", "auser")
                .resource("child",
                        "jcr:property", "prop-value")
                .commit();

        // When
        TestResource model = new TestResource(slingContext.resourceResolver().getResource("/content/module1"));

        // Then
        assertNotNull(model.CHILD.getOrCreate());
        assertEquals("prop-value", model.CHILD.get().getResource().getValueMap().get("jcr:property"));
    }

    @Test
    public void testGetOrCreateChildDoesNotExist() {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module",
                        "jcr:created", Calendar.getInstance(),
                        "jcr:createdBy", "auser")
                .commit();

        // When
        TestResource model = new TestResource(slingContext.resourceResolver().getResource("/content/module1"));

        // Then
        assertNotNull(model.CHILD.getOrCreate());
    }

    @Test
    public void testCreate() throws Exception {
        // Given
        ResourceResolver resourceResolver = slingContext.resourceResolver();
        Resource root = resourceResolver.getResource("/");

        // When
        TestResource model = new TestResource(
                resourceResolver.create(root,"test", null));
        model.NAME.set("my-new-node");
        model.NUMBER.set(25L);
        model.commit();

        // Then
        Resource storedResource = resourceResolver.getResource("/test");
        TestResource storedModel = new TestResource(storedResource);
        assertEquals("my-new-node", storedModel.NAME.get());
        assertEquals(new Long(25), storedModel.NUMBER.get());
    }

    @Test
    public void simpleToMap() {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:name", "my-module",
                        "jcr:date", Calendar.getInstance(),
                        "jcr:number", 26,
                        "jcr:boolean", true,
                        "jcr:stringArray", stringArrayValue)
                .commit();

        // When
        Map<String, Object> map = new TestResource(slingContext.resourceResolver().getResource("/content/module1")).toMap();

        // Then
        assertEquals("my-module", map.get("jcr:name"));
        assertEquals(26L, map.get("jcr:number"));
        assertTrue(map.containsKey("jcr:date"));
        assertTrue(map.containsKey("jcr:boolean"));
        assertEquals(true, map.get("jcr:boolean"));
        assertTrue(map.containsKey("jcr:stringArray"));
        assertArrayEquals(stringArrayValue, (String[])map.get("jcr:stringArray"));
    }

    @Test
    public void toMapWithExclusions() {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:name", "my-module",
                        "jcr:date", Calendar.getInstance(),
                        "jcr:number", 26)
                .commit();

        // When
        Map<String, Object> map = new TestResource(slingContext.resourceResolver().getResource("/content/module1"))
                .toMap("jcr:name", "jcr:date");

        // Then
        assertFalse(map.containsKey("jcr:name"));
        assertFalse(map.containsKey("jcr:date"));
        assertTrue(map.containsKey("jcr:number"));
    }

    @Test void testInitDefaultValues() {
        // Given
        slingContext.build()
                .resource("/content/test")
                .commit();

        // When
        Grandchild grandChild = new TestResource(slingContext.resourceResolver().getResource("/content/test"))
                .CHILD.getOrCreate()
                .GRANDCHILD.getOrCreate();

        // Then
        // default values are set
        assertEquals("DEFAULT_VALUE", grandChild.DEFAULT_VAL.get());
        // non-default values aren't
        assertFalse(grandChild.NAME.isSet());
    }

    public static class TestResource extends SlingResource {

        public final Field<String> NAME = new Field<>(String.class, "jcr:name");
        public final Field<Calendar> DATE = new Field<>(Calendar.class, "jcr:date");
        public final Field<Long> NUMBER = new Field<>(Long.class, "jcr:number");
        public final Field<Boolean> BOOLEAN = new Field<>(Boolean.class, "jcr:boolean");
        public final Field<String[]> STRINGARRAY = new Field<>(String[].class, "jcr:stringArray");

        public final DeepField<String> GRANDCHILD_NAME = new DeepField<>(String.class, "child/grandchild/jcr:name");

        public final ChildResource<Child> CHILD = new ChildResource<>(Child.class, "child");

        public TestResource(Resource resource) {
            super(resource);
        }
    }

    public static class Child extends SlingResource {
        public final ChildResource<Grandchild> GRANDCHILD = new ChildResource<>(Grandchild.class, "grandchild");

        public Child(Resource resource) {
            super(resource);
        }
    }

    public static class Grandchild extends SlingResource {

        public final Field<String> NAME = new Field<>(String.class, "jcr:name");
        public final Field<String> DEFAULT_VAL = new Field<>(String.class, "jcr:defaulted", "DEFAULT_VALUE");

        public Grandchild(Resource resource) {
            super(resource);
        }
    }
}