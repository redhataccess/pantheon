package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import java.util.Iterator;

/**
 * A decorator for Sling's {@link org.apache.sling.api.resource.Resource} objects. It adds convenience methods
 * unavailable in the original class. It implements the SlingModel interface by default.
 *
 * @author Carlos Munoz
 */
public class ResourceDecorator implements SlingModel {

    static final String DEFAULT_PRIMARY_TYPE = "nt:unstructured";
    static final String[] DEFAULT_MIXINS = new String[]{};

    private final Resource wrapped;

    public ResourceDecorator(Resource wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public <T extends SlingModel> Child<T> child(String name, Class<T> type) {
        return new ChildImpl<>(name, type, this);
    }

    @Override
    public <T> Field<T> field(String name, Class<T> type) {
        if(type.isEnum()) {
            // NOTE: This is some ugly casting magic so that the method is able to
            // return the right types
            return (Field<T>) enumField(name, (Class<? extends Enum>)type);
        }
        return new FieldImpl<>(name, type, this);
    }

    private Field<? extends Enum> enumField(String name, Class<? extends Enum> type) {
        return new EnumFieldImpl<>(name, type, this);
    }

    @Override
    public <T extends SlingModel> Reference<T> reference(String name, Class<T> type) {
        return new ReferenceFieldImpl<>(name, type, this);
    }

    @Override
    public void delete() throws PersistenceException {
        wrapped.getResourceResolver().delete(this);
    }

    /*
     * The methods below are all delegate methods around the wrapped resource
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
