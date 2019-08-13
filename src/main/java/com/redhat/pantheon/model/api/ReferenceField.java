package com.redhat.pantheon.model.api;

import javax.jcr.*;

/**
 * Specific implementation of a field of type reference.
 * Referecne fields add convenience to fetch the referenced resource.
 * @param <T>
 */
public class ReferenceField<T extends SlingResource> extends Field<String> {

    private final Class<T> referenceType;

    ReferenceField(String name, Class<T> referenceType, SlingResource owner) {
        super(name, String.class, owner);
        this.referenceType = referenceType;
    }

    /**
     * @see Field#set(Object)
     */
    @Override
    public void set(String value) {
        try {
            owner.adaptTo(Node.class)
                    .setProperty(name, value, PropertyType.REFERENCE);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The referenced object as a {@link SlingResource}, or null if the reference is null or
     * invalid
     * @throws RepositoryException If there was a proble
     */
    public T getReference() throws RepositoryException {
        if(get() == null) {
            return null;
        }

        try {
            Node node = owner.getResourceResolver()
                    .adaptTo(Session.class)
                    .getNodeByIdentifier(this.get());
            return SlingResourceUtil.toSlingResource(
                    owner.getResourceResolver().getResource(node.getPath()),
                    referenceType);
        } catch (ItemNotFoundException infe) {
            return null;
        }
    }
}
