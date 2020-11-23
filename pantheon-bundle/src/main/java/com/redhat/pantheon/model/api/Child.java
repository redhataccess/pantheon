package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.api.util.ResourceTraversal;

import javax.jcr.RepositoryException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A strongly typed child resource definition for a {@link SlingModel}.
 * Child definitions have a reference to their owning object so they
 * can read and modify said owner when necessary.
 *
 * @param <T>
 * @author Carlos Munoz
 */
public interface Child<T extends SlingModel> extends Supplier<T> {

    /**
     * Returns the child as indicated by the definition's name, creating it
     * in the process if necessary.
     * @return The child resource as indicated by this definition
     */
    default T getOrCreate() {
        if(!isPresent()) {
            return create();
        }
        else {
            return get();
        }
    }

    /**
     * Attempts to create the child as indicated by this definition. This might
     * throw an exception if the child already exists.
     * @return The newly created child resource
     */
    T create();

    /**
     * Indicates if the child exists.
     * @return True, if the child exists. False otherwise.
     */
    default boolean isPresent() {
        return get() != null;
    }

    /**
     * Convert this Child to an {@link Optional}
     * @return An {@link Optional} with the contained value.
     */
    default Optional<T> asOptional() {
        return Optional.ofNullable(get());
    }

    /**
     * Navigates to the {@link Child} provided by an accessor function.
     * @param childAccessor A function which given a {@link SlingModel} type, will
     *                      yield a child
     * @param <R>
     * @return A {@link Child} (may be non-existent) as indicated by the accessor.
     */
    default <R extends SlingModel> Child<R> toChild(Function<? super T, Child<R>> childAccessor) {
        if(isPresent()) {
            return childAccessor.apply(get());
        }
        return (Child<R>) NullObjects.nullChild();
    }

    /**
     * Navigates to the {@link Field} produced by an accessor function.
     * @param fieldAccessor A function which given a {@link SlingModel} type, will
     *                      yield a {@link Field}.
     * @param <R>
     * @return A {@link Field} (may be non-present) as indicated by the accessor
     */
    default <R> Field<R> toField(Function<? super T, Field<R>> fieldAccessor) {
        if (isPresent()) {
            return fieldAccessor.apply(get());
        }
        return (Field<R>) NullObjects.nullField();
    }

    /**
     * Navigates to a {@link Child} referenced by a field of type REFERENCE.
     * @param refAccessor A function which given a {@link SlingModel} type, will
     *                    yield a {@link Reference} field.
     * @param <R>
     * @return The {@link Child} object as referenced by the resulting {@link Reference} field
     */
    default <R extends SlingModel> Child<R> toReference(Function<? super T, Reference<R>> refAccessor) {
        if (isPresent()) {
            T childNode = get();
            Reference<R> reference = refAccessor.apply(childNode);
            R refdNode = null;
            try {
                refdNode = reference.getReference();
            } catch (RepositoryException e) {
                // TODO Log a warning
            }
            if(refdNode == null) {
                return (Child<R>) NullObjects.nullChild();
            }

            final R finalRefdNode = refdNode; // Need a final variable so the anonymous class doesn't complain below
            return new Child<R>() {
                @Override
                public R create() {
                    throw new UnsupportedOperationException("This child was created with a specific resource reference," +
                            " hence it cannot be created");
                }

                @Override
                public R get() {
                    return finalRefdNode;
                }
            };
        }
        return (Child<R>) NullObjects.nullChild();
    }
}
