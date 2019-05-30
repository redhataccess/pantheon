package com.redhat.pantheon.model;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class JcrModel implements Adaptable {

    private final Resource resource;

    public JcrModel(@Nonnull Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    protected Stream<Field> getFields() {
        return Arrays.stream(this.getClass().getDeclaredFields())
                // only fields of type JcrModel.Field
                .filter(field -> field.getType() == Field.class)
                // convert to the field values
                .map(field -> {
                    try {
                        return (JcrModel.Field)field.get(JcrModel.this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                // only fields with a non-null value
                .filter(jcrField -> jcrField != null);
    }

    protected Stream<ChildResource> getChildResources() {
        return Arrays.stream(this.getClass().getDeclaredFields())
                // only fields of type JcrModel.ChildResource
                .filter(field -> field.getType() == ChildResource.class)
                // convert to the field values
                .map(field -> {
                    try {
                        return (JcrModel.ChildResource)field.get(JcrModel.this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                // only fields with a non-null value
                .filter(jcrChildRes -> jcrChildRes != null);
    }

    private Map<String, Object> collectDefaultFields() {
        return getFields()
                // only fields with a default value
                .filter(jcrField -> jcrField.defaultValue.isPresent())
                // as a map
                .collect(toMap(
                        f -> f.getName(),
                        f -> f.defaultValue.get()));
    }

    public void commit() throws PersistenceException {
        this.getResource()
                .getResourceResolver()
                .commit();
    }

    @Override
    public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> type) {
        return resource.adaptTo(type);
    }

    public final class Field<TYPE> implements Supplier<TYPE> {

        private final Class<TYPE> fieldType;
        private final String name;
        private final Optional<TYPE> defaultValue;

        public Field(Class<TYPE> fieldType, String name, TYPE defaultValue) {
            this.fieldType = fieldType;
            this.name = name;
            this.defaultValue = Optional.ofNullable(defaultValue);
        }

        public Field(Class<TYPE> fieldType, String name) {
            this(fieldType, name, null);
        }

        String getName() {
            return name;
        }

        private Class<TYPE> getFieldType() {
            return fieldType;
        }

        public TYPE get() {
            return JcrModel.this.getResource()
                    .getValueMap()
                    .get(getName(), getFieldType());
        }

        public void set(TYPE value) {
            JcrModel.this.getResource()
                    .adaptTo(ModifiableValueMap.class)
                    .put(this.name, value);
        }
    }

    /*public final class SyntheticField<TYPE> implements Supplier<TYPE> {

        private final String name;
        private final Function<Resource, TYPE> generator;

        public SyntheticField(@Nonnull String name, @Nonnull Function<Resource, TYPE> generator) {
            this.name = name;
            this.generator = generator;
        }

        public TYPE get() {
            return generator.apply(JcrModel.this.getResource());
        }
    }*/

    public final class ChildResource<RESOURCETYPE extends JcrModel> implements Supplier<RESOURCETYPE> {

        private final Class<RESOURCETYPE> resourceType;
        private final String name;

        public ChildResource(Class<RESOURCETYPE> resourceType, String name) {
            this.resourceType = resourceType;
            this.name = name;
        }

        public Class<RESOURCETYPE> getResourceType() {
            return resourceType;
        }

        public String getName() {
            return name;
        }

        public RESOURCETYPE get() {
            Resource childResource = JcrModel.this.getResource().getChild(this.name);

            // the resource type should have a one arg constructor which takes a resource
            RESOURCETYPE childModel = null;
            try {
                childModel = resourceType.getConstructor(Resource.class)
                        .newInstance(childResource);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Unable to construct new instance of child model", e);
            }
            return childModel;
        }

        public RESOURCETYPE getOrCreate() {
            Resource parent = JcrModel.this.getResource();
            if(!this.exists()) {
                ResourceResolver resourceResolver = parent.getResourceResolver();
                try {
                    resourceResolver.create(parent, name, null);
                } catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }
            return get();
        }

        public boolean exists() {
            Resource parent = JcrModel.this.getResource();
            return parent.getChild(name) != null;
        }
    }
}
