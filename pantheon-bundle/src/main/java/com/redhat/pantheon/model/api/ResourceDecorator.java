package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import java.util.Iterator;

import static com.redhat.pantheon.model.api.SlingModels.createModel;
import static com.redhat.pantheon.model.api.SlingModels.getModel;

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
    public <T extends SlingModel> T createChild(final String name, Class<T> modelType) {
        return createModel(wrapped, name, modelType);
    }

    @Override
    public <T extends SlingModel> T getChild(String name, Class<T> type) {
        return getModel(getChild(name), type);
    }

    @Override
    public <T extends SlingModel> T getOrCreateChild(String name, Class<T> type) {
        if(wrapped.getChild(name) == null) {
            return createChild(name, type);
        }
        return getChild(name, type);
    }

    @Override
    public <T> T getProperty(String name, Class<T> type) {
        return wrapped.getValueMap().get(name, type);
    }

    @Override
    public void setProperty(String name, Object value) {
        wrapped.adaptTo(ModifiableValueMap.class).put(name, value);
    }

    @Override
    public void delete() throws PersistenceException {
        wrapped.getResourceResolver().delete(this);
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
