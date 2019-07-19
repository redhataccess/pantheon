package com.redhat.pantheon.model.api;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.Supplier;

import static com.redhat.pantheon.model.api.SlingResourceUtil.createNewSlingResource;
import static com.redhat.pantheon.model.api.SlingResourceUtil.toSlingResource;

/**
 * A strongly typed child resource definition for a {@link SlingResource}.
 * Child definitions have a reference to their owning object so they
 * can read and modify said owner when necessary.
 *
 * @param <T>
 * @author Carlos Munoz
 */
public class Child<T extends SlingResource> implements Supplier<T> {

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

    /**
     * Returns the child resource by the definition's name, cast to the
     * type indicated in this definition.
     */
    @Override
    public T get() {
        return toSlingResource(owner.getChild(name), type);
    }

    /**
     * Returns the child as indicated by the definition's name, creating it
     * in the process if necessary.
     * @return The child resource as indicated by this definition
     */
    public T getOrCreate() {
        if(owner.getChild(name) == null) {
            return create();
        }
        return get();
    }

    /**
     * Attempts to create the child as indicated by this definition. This might
     * throw an exception if the child already exists.
     * @return The newly created child resource
     */
    public T create() {
        Map<String, Object> propsMap = Maps.newHashMap();
        propsMap.put("jcr:primaryType", primaryType);
        return createNewSlingResource(owner, name, propsMap, type);
    }
}
