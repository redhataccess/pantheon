package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.ModifiableValueMap;

import javax.annotation.Nullable;

/**
 * Default implementation of the {@link Field} interface.
 * A strongly typed jcr field definition for a {@link SlingModel}.
 * Field definitions have a reference to their owning model object so they
 * can read and modify said owner when necessary.
 *
 * @author Carlos Munoz
 */
public class FieldImpl<T> implements Field<T> {

    private final String name;
    private final Class<T> type;
    private final SlingModel owner;

    // TODO Make this protected
    public FieldImpl(String name, Class<T> type, SlingModel owner) {
        this.name = name;
        this.type = type;
        this.owner = owner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    /**
     * @return The field's value from the underlying resource.
     */
    @Override
    public T get() {
        return owner.getValueMap().get(name, type);
    }

    /**
     * Sets the value on the jcr field of the underlying resource.
     * Setting a field to null effectively removes the field from the resource.
     * @param value
     */
    @Override
    public void set(@Nullable T value) {
        ModifiableValueMap mvm = owner.adaptTo(ModifiableValueMap.class);
        if(mvm == null) {
            throw new RuntimeException("Cannot modify resource at " + owner.getPath()
                    + ": This may be due to denied write  permissions");
        }

        if(value == null) {
            mvm.remove(name);
        }
        else {
            mvm.put(name, value);
        }
    }
}
