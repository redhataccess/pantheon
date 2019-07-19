package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.FileResource;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sling.api.resource.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.model.api.SlingResourceUtil.toSlingResource;

/**
 * An implementation of Sling's {@link Resource}, which wraps around an existing reource and
 * provides additional flexibility for use in domain services. For example, the ability to access
 * children, and modify properties directly on the object.
 *
 * It also adds the possibility to create strongly-typed fields and child resources to prevent
 * the proliferation of magic strings in the code, as well as a defined node structure when
 * required.
 *
 * @author Carlos Munoz
 */
public class SlingResource implements Resource {

    private static final String DEFAULT_PRIMARY_TYPE = "nt:unstructured";

    private final Resource wrapped;

    public SlingResource(Resource wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Creates a child resource given a name and a set of initial properties
     * @param name The child's name
     * @param props The initial properties to be stored on the child resource
     * @return The newly created child resource
     * @throws PersistenceException If there is a problem creating the child resource
     */
    public SlingResource createChild(String name, Pair<String, ?>... props) throws PersistenceException {
        Map<String, Object> propsMap =
                Arrays.stream(props).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        return new SlingResource(wrapped.getResourceResolver().create(wrapped, name, propsMap));
    }

    /**
     * Returns a child resource
     * @param name The child's name
     * @param type The type of SlingResource to interpret it as
     * @param <T>
     * @return The child resource or null if one doesn't exist by the given name
     */
    public <T extends SlingResource> T getChild(String name, Class<T> type) {
        return toSlingResource(getChild(name), type);
    }

    /**
     * Returns a child resource, creating it if it doesn't exist.
     * @param name The child's name
     * @param props The properties to set on the child only if being created.
     * @return The found or created child resource
     * @throws PersistenceException If there is a problem creating the new child resource
     */
    public SlingResource getOrCreateChild(String name, Pair<String, ?>... props) throws PersistenceException {
        if(wrapped.getChild(name) == null) {
            return createChild(name, props);
        }
        return new SlingResource(wrapped.getChild(name));
    }

    /**
     * Attempts to create a child resource. This method might throw an exception if there are
     * problems creating the new child resource.
     * @param child The child resource definition
     * @param <T>
     * @return The newly create child resource.
     */
    public <T extends SlingResource> T createChild(Child<T> child) {
        Map<String, Object> props = newHashMap();
        props.put("jcr:primaryType", child.getPrimaryType());
        return SlingResourceUtil.createNewSlingResource(wrapped, child.getName(), props, child.getType());
    }

    /**
     * Returns a child resource, creating it if it doesn't exist.
     * @param child The child resource definition
     * @param <T> The found or created child resource
     * @return The found or created child resource.
     */
    public <T extends SlingResource> T getOrCreateChild(Child<T> child) {
        if(isPresent(child)) {
            return toSlingResource(wrapped.getChild(child.getName()), child.getType());
        }
        return createChild(child);
    }

    /**
     * Returns a resource's property
     * @param name The name of the property
     * @param type The type into which to cast the property value
     * @param <T>
     * @return The property's value, or null if no such property exists
     */
    public <T> T getProperty(String name, Class<T> type) {
        return wrapped.getValueMap().get(name, type);
    }

    /**
     * Indicates if a child exists
     * @param child The child resource definition
     * @return True if a child based on the definition's name exists, false otherwise.
     */
    public boolean isPresent(Child<?> child) {
        return wrapped.getChild(child.getName()) != null;
    }

    /**
     * Creates a new field definition for this resource
     * @param name Field name
     * @param type Field type
     * @param <T>
     * @return A new field definition for this SlingResource.
     */
    protected <T> Field<T> field(String name, Class<T> type) {
        return new Field<>(name, type, this);
    }

    /**
     * Creates a new String-typed field definition for this resource
     * @param name Field name
     * @return A new String-typed field definition for this SlingResource.
     */
    protected Field<String> stringField(String name) {
        return new Field<>(name, String.class, this);
    }

    /**
     * Creates a new Calendar-typed field definition for this resource
     * @param name Field name
     * @return A new Calendar-typed field definition for this SlingResource.
     */
    protected Field<Calendar> dateField(String name) {
        return new Field<>(name, Calendar.class, this);
    }

    /**
     * Creates a new child resource definition for this resource
     * @param name child resource name
     * @param type the {@link SlingResource} type
     * @param primaryType The jcr primary type to assign to the new child (important if relying on JCR to auto-assign
     *                    values, or auto-create nodes.
     * @param <T>
     * @return A new child definition for this SlingResource
     */
    protected <T extends SlingResource> Child<T> child(String name, Class<T> type, String primaryType) {
        return new Child<>(name, type, primaryType, this);
    }

    /**
     * Creates a new child resource definition for this resource.
     * The primary type of the created node is determined by the DEFAULT_PRIMARY_TYPE property.
     * @param name child resource name
     * @param type the {@link SlingResource} type
     * @param <T>
     * @return A new child definition for this SlingResource
     */
    protected <T extends SlingResource> Child<T> child(String name, Class<T> type) {
        return child(name, type, DEFAULT_PRIMARY_TYPE);
    }

    /**
     * Creates a new File-typed resource definition for this resource.
     * File resources contain a very specific structure.
     * @param name child resource name
     * @param <T>
     * @return A new File-type child definition for this SlingResource
     */
    protected Child<FileResource> file(String name) {
        return child(name, FileResource.class, "nt:file");
    }

    /*
     * The methods below are all delgate methods around the wrapped resource
     * to make sure SlingResource conforms to the Resource interface.
     */

    @Override
    public String getPath() {
        return wrapped.getPath();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public Resource getParent() {
        return wrapped.getParent();
    }

    @Override
    public Iterator<Resource> listChildren() {
        return wrapped.listChildren();
    }

    @Override
    public Iterable<Resource> getChildren() {
        return wrapped.getChildren();
    }

    @Override
    public Resource getChild(String relPath) {
        return wrapped.getChild(relPath);
    }

    @Override
    public String getResourceType() {
        return wrapped.getResourceType();
    }

    @Override
    public String getResourceSuperType() {
        return wrapped.getResourceSuperType();
    }

    @Override
    public boolean hasChildren() {
        return wrapped.hasChildren();
    }

    @Override
    public boolean isResourceType(String resourceType) {
        return wrapped.isResourceType(resourceType);
    }

    @Override
    public ResourceMetadata getResourceMetadata() {
        return wrapped.getResourceMetadata();
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return wrapped.getResourceResolver();
    }

    @Override
    public ValueMap getValueMap() {
        return wrapped.getValueMap();
    }

    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        return wrapped.adaptTo(type);
    }
}
