package com.redhat.pantheon.model.api.v2;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static com.redhat.pantheon.model.api.v2.SlingModels.getModel;

/**
 * A {@link Field} implementation for JCR reference-typed fields. Adds methods to
 * resolve the reference.
 *
 * @author Carlos Munoz
 */
public class ReferenceFieldImpl<T extends SlingModel> extends FieldImpl<String> implements Reference<T> {

    private final Class<T> referenceType;
    private final SlingModel owner;

    // TODO make this package-protected
    public ReferenceFieldImpl(String name, Class<T> referenceType, SlingModel owner) {
        super(name, String.class, owner);
        this.referenceType = referenceType;
        this.owner = owner;
    }

    /**
     * @see FieldImpl#set(Object)
     */
    @Override
    public void set(String value) {
        try {
            owner.adaptTo(Node.class)
                    .setProperty(getName(), value, PropertyType.REFERENCE);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The referenced object as a {@link SlingModel}, or null if the reference is null or
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
            return getModel(
                    owner.getResourceResolver().getResource(node.getPath()),
                    referenceType);
        } catch (ItemNotFoundException infe) {
            return null;
        }
    }
}
