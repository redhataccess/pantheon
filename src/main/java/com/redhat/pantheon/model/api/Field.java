package com.redhat.pantheon.model.api;

import org.apache.sling.api.resource.ModifiableValueMap;

public class Field<T> {

    private final String name;
    private final Class<T> type;
    private final SlingResource owner;

    Field(String name, Class<T> type, SlingResource owner) {
        this.name = name;
        this.type = type;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    public T get() {
        return owner.getValueMap().get(name, type);
    }

    public void set(T value) {
        owner.adaptTo(ModifiableValueMap.class).put(name, value);
    }
}
