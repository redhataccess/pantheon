package com.redhat.pantheon.model.api;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

/**
 * An Editable Sling resource model object. These are simple objects made up of a single constructor based on an existing
 * sling resource, and a set of fields or child resources mapped to the resource. Fields and child resources are
 * strongly typed and they can be modified. SlingResources are wrappers around existing sling resources and as such the
 * resource needs to exist before it can be wrapped by one of these.
 */
public class SlingResource implements Adaptable {

    private final Resource resource;

    public SlingResource(@Nonnull Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    /**
     * Initializes all fields which have a default value set, and which aren't already set.
     * This method is only meant to be called after new resources are created.
     */
    protected void initDefaultValues() {
        getFields()
                // discard fields which already have a value set
                .filter(field -> !field.isSet())
                // only consider fields which have a default value
                .filter(field -> field.defaultValue.isPresent())
                .forEach(field -> field.set(field.defaultValue.get()));
    }

    /**
     * Returns all the Field typed members for this JCR model object. It will not return
     * non-initialized Field-typed members.
     */
    protected final Stream<Field> getFields() {
        return stream(this.getClass().getDeclaredFields())
                // only fields of type SlingResource.Field
                .filter(field -> field.getType() == Field.class)
                // convert to the field values
                .map(field -> {
                    try {
                        return (SlingResource.Field)field.get(SlingResource.this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                // only initialized fields
                .filter(jcrField -> jcrField != null);
    }

    /**
     * Returns all the DeepField typed members for this JCR model object. It will not return
     * non-initialized DeepField typed members.
     */
    protected final Stream<DeepField> getDeepFields() {
        return stream(this.getClass().getDeclaredFields())
                // only fields of type SlingResource.Field
                .filter(field -> field.getType() == DeepField.class)
                // convert to the field values
                .map(field -> {
                    try {
                        return (SlingResource.DeepField)field.get(SlingResource.this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                // only initialized fields
                .filter(jcrField -> jcrField != null);
    }

    /**
     * Returns all the ChildResource typed members for this JCR model object. It will not return
     * non-initialized ChildResource typed members.
     */
    protected final Stream<ChildResource> getChildResources() {
        return stream(this.getClass().getDeclaredFields())
                // only fields of type SlingResource.ChildResource
                .filter(field -> field.getType() == ChildResource.class)
                // convert to the field values
                .map(field -> {
                    try {
                        return (SlingResource.ChildResource)field.get(SlingResource.this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                // only fields with a non-null value
                .filter(jcrChildRes -> jcrChildRes != null);
    }

    /**
     * Returns a map with all the fields (deep fields included) for this model. The keys for the map
     * are the jcr field names.
     * @param excluding A list of JCR field names to exclude from the returned map.
     * @return
     */
    public Map<String, Object> toMap(String ... excluding) {
        // get the fields first
        Map<String, Object> returnMap = getFields()
                .filter(field ->
                        stream(excluding).noneMatch(arrayField -> arrayField.equals(field.getName())))
                .collect(Collectors.toMap(
                        field -> field.getName(),
                        field -> field.get()
                ));

        // add the deep fields
        returnMap.putAll(
                getDeepFields().filter(deepField ->
                        stream(excluding).noneMatch(arrayField -> arrayField.equals(deepField.getPath())))
                // get rid of null values
                .filter(deepField -> deepField.get() != null)
                .collect(Collectors.toMap(
                        deepField -> deepField.getPath(),
                        deepField -> deepField.get()
                ))
        );

        return returnMap;
    }

    /**
     * Commit any changes made to this resource.
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
        private final String path;

        public DeepField(Class<TYPE> fieldType, String path) {
            this.fieldType = fieldType;
            this.path = path;
        }

        @Override
        public TYPE get() {
            return SlingResource.this.getResource()
                    .adaptTo(ValueMap.class)
                    .get(getPath(), getFieldType());
        }

        private Class<TYPE> getFieldType() {
            return fieldType;
        }

        String getPath() {
            return path;
        }
    }

    /**
     * A simple scalar Field or property in the SlingModel object.
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
            return SlingResource.this.getResource()
                    .getValueMap()
                    .get(getName(), getFieldType());
        }

        public void set(TYPE value) {
            SlingResource.this.getResource()
                    .adaptTo(ModifiableValueMap.class)
                    .put(this.name, value);
        }

        public boolean isSet() {
            return SlingResource.this.getResource()
                    .getValueMap()
                    .containsKey(getName());
        }
    }

    /**
     * A Child resource. Useful to build deep and modifiable content structures.
     * @param <MODELTYPE>
     */
    public final class ChildResource<MODELTYPE extends SlingResource> implements Supplier<MODELTYPE> {

        private final Class<MODELTYPE> modelType;
        private final String name;

        public ChildResource(Class<MODELTYPE> modelType, String name) {
            this.modelType = modelType;
            this.name = name;
        }

        Class<MODELTYPE> getModelType() {
            return modelType;
        }

        public String getName() {
            return name;
        }

        public MODELTYPE get() {
            Resource childResource = SlingResource.this.getResource().getChild(this.name);

            // the resource type should have a one arg constructor which takes a resource
            MODELTYPE childModel = null;
            try {
                childModel = getModelType().getConstructor(Resource.class)
                        .newInstance(childResource);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Unable to construct new instance of child model", e);
            }
            return childModel;
        }

        public SlingResource getParent() {
            return SlingResource.this;
        }

        public MODELTYPE getOrCreate() {
            Resource parent = SlingResource.this.getResource();
            if(!this.isPresent()) {
                ResourceResolver resourceResolver = parent.getResourceResolver();
                try {
                    resourceResolver.create(parent, name, null);
                } catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }
            MODELTYPE model = get();
            model.initDefaultValues();
            return model;
        }

        public boolean isPresent() {
            Resource parent = SlingResource.this.getResource();
            return parent.getChild(name) != null;
        }
    }
}
