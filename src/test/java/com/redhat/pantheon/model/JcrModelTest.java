package com.redhat.pantheon.model;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
@Disabled
class JcrModelTest {

    private final SlingContext slingContext = new SlingContext();

    @Test
    public void fieldMapping() throws Exception {
        slingContext.build()
                .resource("/content/module1",
                        "jcr:name", "my-name",
                        "jcr:date", Calendar.getInstance(),
                        "jcr:number", "10")
                .commit();

        TestModel model = new TestModel(slingContext.resourceResolver().getResource("/content/module1"));

        assertEquals("my-name", model.NAME.get());
        assertEquals(new Long(10), model.NUMBER.get());
        assertNotNull(model.DATE.get());
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
        TestModel model = new TestModel(slingContext.resourceResolver().getResource("/content/module1"));

        // Then
        model.CHILD
                .get()
                .GRANDCHILD
                .get();
    }

    @Test
    public void fieldEditing() throws Exception {
        // Given
        slingContext.build()
                .resource("/content/module1",
                        "jcr:primaryType", "pant:module",
                        "jcr:created", Calendar.getInstance(),
                        "jcr:createdBy", "auser")
                .commit();

        // When
        TestModel model = new TestModel(slingContext.resourceResolver().getResource("/content/module1"));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.YEAR, 2019);
        model.DATE.set(cal);
        model.NAME.set("someoneelse");
        model.NUMBER.set(15L);

        // Then
        assertEquals(1, model.DATE.get().get(Calendar.MONTH));
        assertEquals(1, model.DATE.get().get(Calendar.DATE));
        assertEquals(2019, model.DATE.get().get(Calendar.YEAR));
        assertEquals("someoneelse", model.NAME.get());
        assertEquals(new Long(15), model.NUMBER.get());
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
        TestModel model = new TestModel(slingContext.resourceResolver().getResource("/content/module1"));

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
        TestModel model1 = new TestModel(slingContext.resourceResolver().getResource("/content/module1"));
        TestModel model2 = new TestModel(slingContext.resourceResolver().getResource("/content/module2"));

        // Then
        assertTrue(model1.CHILD.isPresent());
        assertFalse(model2.CHILD.isPresent());
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
        TestModel model = new TestModel(slingContext.resourceResolver().getResource("/content/module1"));

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
        TestModel model = new TestModel(slingContext.resourceResolver().getResource("/content/module1"));

        // Then
        assertNotNull(model.CHILD.getOrCreate());
    }

    @Test
    public void testCreate() throws Exception {
        // Given
        ResourceResolver resourceResolver = slingContext.resourceResolver();
        Resource root = resourceResolver.getResource("/");

        // When
        TestModel model = new TestModel(
                resourceResolver.create(root,"test", null));
        model.NAME.set("my-new-node");
        model.NUMBER.set(25L);
        model.commit();

        // Then
        Resource storedResource = resourceResolver.getResource("/test");
        TestModel storedModel = new TestModel(storedResource);
        assertEquals("my-new-node", storedModel.NAME.get());
        assertEquals(new Long(25), storedModel.NUMBER.get());
    }

    public static class TestModel extends JcrModel {

        public final Field<String> NAME = new Field<>(String.class, "jcr:name");
        public final Field<Calendar> DATE = new Field<>(Calendar.class, "jcr:date");
        public final Field<Long> NUMBER = new Field<>(Long.class, "jcr:number");

        public final ChildResource<Child> CHILD = new ChildResource<>(Child.class, "child");

        public TestModel(Resource resource) {
            super(resource);
        }
    }

    public static class Child extends JcrModel {
        public final ChildResource<Grandchild> GRANDCHILD = new ChildResource<>(Grandchild.class, "grandchild");

        public Child(Resource resource) {
            super(resource);
        }
    }

    public static class Grandchild extends JcrModel {
        public Grandchild(Resource resource) {
            super(resource);
        }
    }
}