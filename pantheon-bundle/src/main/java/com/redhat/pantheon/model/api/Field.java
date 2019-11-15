package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.ModifiableValueMap;

import javax.annotation.Nullable;
import javax.jcr.RepositoryException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A strongly typed jcr field definition for a {@link SlingResource}.
 * Field definitions have a reference to their owning object so they
 * can read and modify said owner when necessary.
 *
 * @param <T>
 * @author Carlos Munoz
 */
public class Field<T> implements com.redhat.pantheon.model.api.v2.Field<T> {

    protected final String name;
    protected final Class<T> type;
    protected final SlingResource owner;

    Field(String name, Class<T> type, SlingResource owner) {
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
     * Sets the default value for this field. The default value is only set if there is
     * no value set on the field
     * @param defVal The default value to use
     * @return The field itself
     */
    @Override
    public Field<T> defaultValue(final T defVal) {
        if(this.get() == null) {
            this.owner.setProperty(this.name, defVal);
        }
        return this;
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
        if(value == null) {
            mvm.remove(name);
        }
        else {
            mvm.put(name, value);
        }
    }
}
