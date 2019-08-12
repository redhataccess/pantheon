package com.redhat.pantheon.model.api;

import javax.jcr.*;

public class ReferenceField<T extends SlingResource> extends Field<String> {

    private final Class<T> referenceType;

    ReferenceField(String name, Class<T> referenceType, SlingResource owner) {
        super(name, String.class, owner);
        this.referenceType = referenceType;
    }

    @Override
    public void set(String value) {
        try {
            owner.adaptTo(Node.class)
                    .setProperty(name, value, PropertyType.REFERENCE);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

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
