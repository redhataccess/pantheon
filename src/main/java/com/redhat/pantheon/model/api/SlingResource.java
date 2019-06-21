package com.redhat.pantheon.model.api;

import com.google.common.collect.Streams;
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
     * Initializes all fields which have a default value, and which aren't already initialized.
     * This method is only meant to be called after new resources are created.
     */
    protected void initDefaultValues() {
        getMembers(Field.class)
                // discard fields which already have a value set
                .filter(field -> !field.isSet())
                // only consider fields which have a default value
                .filter(field -> field.defaultValue.isPresent())
                .forEach(field -> field.set(field.defaultValue.get()));
    }

    /**
     * Returns all SlingModel members (assigned fields which implement the ResourceMember interface
     * @return A stream with all resource members in this object.
     */
    private Stream<ResourceMember> allMembers() {
        return stream(this.getClass().getDeclaredFields())
                // only class fields which implement ResourceMember
                .filter(reflectedField -> ResourceMember.class.isAssignableFrom(reflectedField.getType()))
                // convert to big-Field values
                .map(field -> {
                    try {
                        return (ResourceMember) field.get(SlingResource.this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                // only initialized fields
                .filter(field -> field != null);
    }

    /**
     * Returns all SlingModel members of a specific implementation.
     */
    private <A extends ResourceMember> Stream<A> getMembers(@Nonnull Class<A> memberClass) {
        return (Stream<A>) allMembers()
                .filter(resourceMember -> memberClass.isAssignableFrom(resourceMember.getClass()));
    }

    /**
     * Returns a map with all the fields (deep fields included) for this model. The keys for the map
     * are the jcr field names.
     *
     * @param excluding A list of JCR field names to exclude from the returned map.
     * @return
     */
    public Map<String, Object> toMap(String ... excluding) {
        return Streams.concat(getMembers(Field.class), getMembers(DeepField.class))
                // ignore null values
                .filter(accessor -> accessor.get() != null)
                // ignore the listed names
                .filter(accessor ->
                        stream(excluding).noneMatch(arrayField -> arrayField.equals(accessor.getName())))
                // convert to a map
                .collect(Collectors.toMap(
                        ResourceMember::getName,
                        Supplier::get
                ));
    }

    /**
     * Commit any changes made to this resource.
     *
     * @throws PersistenceException If there is a problem making the changes
     */
    public void commit() throws PersistenceException {
        this.getResource()
                .getResourceResolver()
                .commit();
    }

    /**
     * Calls the {@link Resource#adaptTo(Class)} method on the wrapped resource.
     *
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
     *
     * @param <TYPE>
     */
    public final class DeepField<TYPE> implements Accessor<TYPE> {

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

        @Override
        public String getName() {
            return getPath();
        }
    }

    /**
     * A simple scalar Field or property in the SlingModel object.
     *
     * @param <TYPE> The type of the field to map.
     */
    public final class Field<TYPE> implements Accessor<TYPE>, Mutator<TYPE> {

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

        @Override
        public String getName() {
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
     *
     * @param <MODELTYPE>
     */
    public final class ChildResource<MODELTYPE extends SlingResource> implements Accessor<MODELTYPE> {

        private final Class<MODELTYPE> modelType;
        private final String name;

        public ChildResource(Class<MODELTYPE> modelType, String name) {
            this.modelType = modelType;
            this.name = name;
        }

        Class<MODELTYPE> getModelType() {
            return modelType;
        }

        @Override
        public String getName() {
            return name;
        }

        public MODELTYPE get() {
            Resource childResource = SlingResource.this.getResource().getChild(this.name);

            if(childResource == null) {
                return null;
            }

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
            if (!this.isPresent()) {
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
