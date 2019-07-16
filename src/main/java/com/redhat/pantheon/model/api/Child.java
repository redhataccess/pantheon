package com.redhat.pantheon.model.api;

import com.google.common.collect.Maps;

import java.util.Map;

import static com.redhat.pantheon.model.api.SlingResourceUtil.createNewSlingResource;
import static com.redhat.pantheon.model.api.SlingResourceUtil.toSlingResource;

public class Child<T extends SlingResource> {

    private final String name;
    private final Class<T> type;
    private final String primaryType;
    private final SlingResource owner;

    Child(String name, Class<T> type, String primaryType, SlingResource owner) {
        this.name = name;
        this.type = type;
        this.primaryType = primaryType;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    public String getPrimaryType() {
        return primaryType;
    }

    public T get() {
        return toSlingResource(owner.getChild(name), type);
    }

    public T getOrCreate() {
        if(owner.getChild(name) == null) {
            return create();
        }
        return get();
    }

    public T create() {
        Map<String, Object> propsMap = Maps.newHashMap();
        propsMap.put("jcr:primaryType", primaryType);
        return createNewSlingResource(owner, name, propsMap, type);
    }
}
