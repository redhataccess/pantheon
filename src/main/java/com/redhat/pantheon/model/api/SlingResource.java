package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.FileResource;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sling.api.resource.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.model.api.SlingResourceUtil.toSlingResource;

public class SlingResource implements Resource {

    private static final String DEFAULT_PRIMARY_TYPE = "nt:unstructured";

    private final Resource wrapped;

    public SlingResource(Resource wrapped) {
        this.wrapped = wrapped;
    }

    public SlingResource createChild(String name, Pair<String, ?>... props) throws PersistenceException {
        Map<String, Object> propsMap =
                Arrays.stream(props).collect(Collectors.toMap(p -> p.getLeft(), p -> p.getRight()));
        return new SlingResource(wrapped.getResourceResolver().create(wrapped, name, propsMap));
    }

    public <T extends SlingResource> T getChild(String name, Class<T> type) {
        return toSlingResource(getChild(name), type);
    }

    public SlingResource getOrCreateChild(String name, Pair<String, ?>... props) throws PersistenceException {
        if(wrapped.getChild(name) == null) {
            return createChild(name, props);
        }
        return new SlingResource(wrapped.getChild(name));
    }

    public <T extends SlingResource> T createChild(Child<T> child) {
        Map<String, Object> props = newHashMap();
        props.put("jcr:primaryType", child.getPrimaryType());
        return SlingResourceUtil.createNewSlingResource(wrapped, child.getName(), props, child.getType());
    }

    public <T extends SlingResource> T getOrCreateChild(Child<T> child) {
        if(isPresent(child)) {
            return toSlingResource(wrapped.getChild(child.getName()), child.getType());
        }
        return createChild(child);
    }

    public <T> T getProperty(String name, Class<T> type) {
        return wrapped.getValueMap().get(name, type);
    }

    public boolean isPresent(Child<?> child) {
        return wrapped.getChild(child.getName()) != null;
    }

    public <T> Field<T> field(String name, Class<T> type) {
        return new Field<>(name, type, this);
    }

    public Field<String> stringField(String name) {
        return new Field<>(name, String.class, this);
    }

    public Field<Calendar> dateField(String name) {
        return new Field<>(name, Calendar.class, this);
    }

    public <T extends SlingResource> Child<T> child(String name, Class<T> type, String primaryType) {
        return new Child<>(name, type, primaryType, this);
    }

    public <T extends SlingResource> Child<T> child(String name, Class<T> type) {
        return child(name, type, DEFAULT_PRIMARY_TYPE);
    }

    public Child<FileResource> file(String name) {
        return child(name, FileResource.class, "nt:file");
    }

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
