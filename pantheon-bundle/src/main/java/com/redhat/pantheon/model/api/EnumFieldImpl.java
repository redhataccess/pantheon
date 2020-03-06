package com.redhat.pantheon.model.api;

import javax.annotation.Nullable;

/**
 * Specific {@link Field} implementation to deal with Enumerated properties.
 * This implementation saves the values as the enumeration value's name in the JCR
 * repository.
 *
 * @author Carlos Munoz
 */
public class EnumFieldImpl<T extends Enum> extends FieldImpl<T> {

    private final SlingModel owner;

    // TODO make this package-protected
    public EnumFieldImpl(String name, Class<T> type, SlingModel owner) {
        super(name, type, owner);
        this.owner = owner;
    }

    @Override
    public void set(@Nullable T value) {
        new FieldImpl<>(getName(), String.class, owner).set( value == null ? null : value.name() );
    }

    @Override
    public T get() {
        FieldImpl<String> fieldImpl = new FieldImpl<>(getName(), String.class, owner);
        if(fieldImpl.get() == null) {
            return null;
        }
        return (T)Enum.valueOf(getType(), fieldImpl.get());
    }
}
