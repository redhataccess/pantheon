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
    public void createChild() throws Exception {
        // Given
        slingContext.build()
                .resource("/node")
                .commit();
        SlingResource model = new SlingResource(slingContext.resourceResolver().getResource("/node"));

        // When
        SlingResource child = model.createChild("child", SlingResource.class);
        SlingResource grandchild = model.createChild("/grand/child", SlingResource.class);

        // Then
        assertNotNull(child);
        assertThrows(RuntimeException.class, () -> model.createChild("child", SlingResource.class), "Same child cannot be created twice");
        assertNotNull(grandchild);
    }

    @Test
    public void createChildUsingDefinition() {
        // Given
        slingContext.build()
                .resource("/node")
                .commit();
        SlingResource model = new SlingResource(slingContext.resourceResolver().getResource("/node"));

        // When
        SlingResource child = model.createChild(model.child("child", SlingResource.class));
        SlingResource grandchild = model.createChild(model.child("/grand/child", SlingResource.class));


        // Then
        assertNotNull(child);
        assertThrows(RuntimeException.class, () -> model.createChild(model.child("child", SlingResource.class)),
                "Same child cannot be created twice");
        assertNotNull(grandchild);
    }

    @Test
    public void getChild() {
        // Given
        slingContext.build()
                .resource("/node/child")
                .commit();
        SlingResource model = new SlingResource(slingContext.resourceResolver().getResource("/node"));

        // When
        SlingResource child = model.getChild("child", SlingResource.class);

        // Then
        assertNotNull(child);
        assertNull(model.getChild("non-existent", SlingResource.class));
    }

    @Test
    public void getOrCreateChild() {
        // Given
        slingContext.build()
                .resource("/node/child")
                .commit();
        SlingResource model = new SlingResource(slingContext.resourceResolver().getResource("/node"));

        // When
        SlingResource child = model.getOrCreateChild("child", SlingResource.class);
        SlingResource nonExistentChild = model.getOrCreateChild("new-child", SlingResource.class);

        // Then
        assertNotNull(child);
        assertNotNull(nonExistentChild);
    }

    @Test
    public void getOrCreateChildWithDefinition() {
        // Given
        slingContext.build()
                .resource("/node/child")
                .commit();
        SlingResource model = new SlingResource(slingContext.resourceResolver().getResource("/node"));

        // When
        SlingResource child = model.getOrCreateChild(model.child("child", SlingResource.class));
        SlingResource nonExistentChild = model.getOrCreateChild(model.child("new-child", SlingResource.class));

        // Then
        assertNotNull(child);
        assertNotNull(nonExistentChild);
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
        assertEquals("prop-value", model.CHILD.get().getProperty("jcr:property", String.class));
    }

    @Test
    public void testGetChildNotPresent() throws Exception {
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
        assertNull(model.CHILD.get());
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
        assertNotNull(model1.CHILD.get() != null);
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
        assertEquals("prop-value", model.CHILD.get().getProperty("jcr:property", String.class));
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
        model.getResourceResolver().commit();

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
                        "jcr:number", 26L,
                        "jcr:boolean", true,
                        "jcr:stringArray", stringArrayValue)
                .commit();

        // When
        Map<String, Object> map = new TestResource(slingContext.resourceResolver().getResource("/content/module1")).getValueMap();

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
    public void fieldDefaultValue() {
        // Given
        slingContext.build()
                .resource("/content/test")
                .commit();

        // When
        TestResource resource = new TestResource(slingContext.resourceResolver().getResource("/content/test"));

        // Then
        assertEquals("default", resource.STRING_WITH_DEFAULT.get());
    }

    public static class TestResource extends SlingResource {

        public final Field<String> NAME = stringField("jcr:name");
        public final Field<Calendar> DATE = dateField("jcr:date");
        public final Field<Long> NUMBER = field("jcr:number", Long.class);
        public final Field<Boolean> BOOLEAN = field("jcr:boolean", Boolean.class);
        public final Field<String[]> STRINGARRAY = field("jcr:stringArray", String[].class);
        public final Field<String> STRING_WITH_DEFAULT = stringField("stringWithDefault").defaultValue("default");

        public final Field<String> GRANDCHILD_NAME = stringField("child/grandchild/jcr:name");

        public final Child<ChildResource> CHILD = child("child", ChildResource.class);

        public TestResource(Resource resource) {
            super(resource);
        }
    }

    public static class ChildResource extends SlingResource {
        public final Field<String> NAME = stringField("jcr:name");

        public final Child<Grandchild> GRANDCHILD = child("grandchild", Grandchild.class);

        public ChildResource(Resource resource) {
            super(resource);
        }
    }

    public static class Grandchild extends SlingResource {

        public final Field<String> NAME = stringField("jcr:name");

        public Grandchild(Resource resource) {
            super(resource);
        }
    }
}