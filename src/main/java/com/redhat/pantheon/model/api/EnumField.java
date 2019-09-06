package com.redhat.pantheon.model.api;

import javax.annotation.Nullable;

/**
 * An enumeration typed field. Only allows values of the given enumeration
 * and will store them in the JCR tree as a string.
 *
 * @author Carlos Munoz
 */
public class EnumField<T extends Enum> extends Field<T> {

    EnumField(String name, Class<T> type, SlingResource owner) {
        super(name, type, owner);
    }

    @Override
    public void set(@Nullable T value) {
        new Field<>(name, String.class, owner).set( value == null ? null : value.name() );
    }

    @Override
    public T get() {
        return (T)Enum.valueOf(type, new Field<>(name, String.class, owner).get());
    }
}
