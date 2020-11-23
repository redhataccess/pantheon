package com.redhat.pantheon.model.api;

import static com.redhat.pantheon.model.api.SlingModels.getModel;

/**
 * Default implementation of the {@link Child} interface.
 * A strongly typed child resource definition for a {@link SlingModel}.
 * Child definitions have a reference to their owning parent so they
 * can read and modify said owner when necessary.
 *
 * @author Carlos Munoz
 */
public class ChildImpl<T extends SlingModel> implements Child<T> {

    private final String name;
    private final Class<T> type;
    private final SlingModel owner;

    // TODO Make this package protected
    public ChildImpl(String name, Class<T> type, SlingModel owner) {
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

    /**
     * Returns the child resource by the definition's name, cast to the
     * type indicated in this definition.
     */
    @Override
    public T get() {
        return getModel(owner.getChild(name), type);
    }

    /**
     * Attempts to create the child as indicated by this definition. This might
     * throw an exception if the child already exists.
     * @return The newly created child resource
     */
    @Override
    public T create() {
        return SlingModels.createModel(owner, name, type);
    }
}
