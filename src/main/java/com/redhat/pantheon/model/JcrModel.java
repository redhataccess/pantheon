package com.redhat.pantheon.model;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.*;
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

/**
 * An Editable JCR model object. These are simple objects made up of a single constructor based on an existing
 * sling resource, and a set of fields or child resources mapped to the resource. Fields and child resources are
 * strongly typed and they can be modified. JcrModels are wrappers around existing sling resources and as such the
 * resource needs to exist before it can be wrapped by a model.
 */
public class JcrModel implements Adaptable {

    private final Resource resource;

    public JcrModel(@Nonnull Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    protected final Stream<Field> getFields() {
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

    protected final Stream<ChildResource> getChildResources() {
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

    /**
     * Commit any changes made to this model.
     * @throws PersistenceException If there is a problem making the changes
     */
    public void commit() throws PersistenceException {
        this.getResource()
                .getResourceResolver()
                .commit();
    }

    /**
     * Calls the {@link Resource#adaptTo(Class)} method on the wrapped resource.
     * @param type
     * @param <AdapterType>
     * @return
     */
    @Override
    public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> type) {
        return resource.adaptTo(type);
    }

    /**
     * A deep field (not directly set on the resource, but instead on a child resource). These fields may be read
     * but not modified as they belong to a different resource. To edit them, use the {@link ChildResource} field
     * type instead.
     * @param <TYPE>
     */
    public final class DeepField<TYPE> implements Supplier<TYPE> {

        private final Class<TYPE> fieldType;
        private final String name;

        public DeepField(Class<TYPE> fieldType, String name) {
            this.fieldType = fieldType;
            this.name = name;
        }

        @Override
        public TYPE get() {
            return JcrModel.this.getResource()
                    .adaptTo(ValueMap.class)
                    .get(getName(), getFieldType());
        }

        private Class<TYPE> getFieldType() {
            return fieldType;
        }

        String getName() {
            return name;
        }
    }

    /**
     * A simple scalar Field or property in the JCR object.
     * @param <TYPE> The type of the field to map.
     */
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

    /**
     * A Child resource. Useful to build deep and modifiable JCR content structures.
     * @param <RESOURCETYPE>
     */
    public final class ChildResource<RESOURCETYPE extends JcrModel> implements Supplier<RESOURCETYPE> {

        private final Class<RESOURCETYPE> resourceType;
        private final String name;

        public ChildResource(Class<RESOURCETYPE> resourceType, String name) {
            this.resourceType = resourceType;
            this.name = name;
        }

        Class<RESOURCETYPE> getResourceType() {
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
                childModel = getResourceType().getConstructor(Resource.class)
                        .newInstance(childResource);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Unable to construct new instance of child model", e);
            }
            return childModel;
        }

        public RESOURCETYPE getOrCreate() {
            Resource parent = JcrModel.this.getResource();
            if(!this.isPresent()) {
                ResourceResolver resourceResolver = parent.getResourceResolver();
                try {
                    resourceResolver.create(parent, name, null);
                } catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }
            return get();
        }

        public boolean isPresent() {
            Resource parent = JcrModel.this.getResource();
            return parent.getChild(name) != null;
        }
    }
}
