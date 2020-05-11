package com.redhat.pantheon.model.api.util;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.SlingModel;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

/**
 * Utility class which offers a null-safe way to traverse deep {@link SlingModel}
 * structures. Traversals represent paths across multiple nodes ending in deeper
 * child nodes, or properties. If any of the intermediary nodes is not present, the whole traversal
 * will still conclude, with a final result of null.<br/>
 * <br/>
 * To use it, invoke the {@link SafeResourceTraversal#start(SlingModel)} method with a
 * model to be traversed. From there on, the whole structure may be traversed all the
 * way down to specific fields.
 *
 * @author Carlos Munoz
 */
public class SafeResourceTraversal<T extends SlingModel> implements Supplier<T> {

    private static final SafeResourceTraversal<?> EMPTY = new SafeResourceTraversal<>(null);

    private final Optional<T> currentResource;

    private SafeResourceTraversal(T resource) {
        this.currentResource = ofNullable(resource);
    }

    /**
     * Starts a safe traversal.
     * @param model The {@link SlingModel} to traverse.
     * @param <M> The Sling Model type to start the traversal
     * @return A traversal object starting from the given {@link SlingModel}. If the model
     * is null, traversals will still conclude but will always yield null results.
     */
    public static final <M extends SlingModel> SafeResourceTraversal<M> start(@Nullable M model) {
        return new SafeResourceTraversal<>(model);
    }

    /**
     * Traverses the current node in the traversal, down to one of its children.
     * @param childAccessor A function that returns a {@link Child} of the current resource.
     * @param <U>
     * @return A resource traversal at the child accessed via the child accessor.
     */
    public <U extends SlingModel> SafeResourceTraversal<U> traverse(Function<? super T, Child<U>> childAccessor) {
        if(currentResource.isPresent()) {
            Child<U> nextTraversalChild = childAccessor.apply(currentResource.get());
            return new SafeResourceTraversal<>(nextTraversalChild.get());
        }
        return (SafeResourceTraversal<U>) EMPTY;
    }

    /**
     * Traverses to a field in the current traversed node. This represents and end
     * to the traversal as there is nothing to traverse from a field.
     * @param fieldAccessor A function that returns a {@link Field} from the current resource.
     * @param <F> The type of the field to access
     * @return An optional containing the value of the field. If the field is not present, or if
     * any of the intermediary nodes in the traversal was not present, this optional is empty.
     */
    public <F> Optional<F> field(Function<? super T, Field<F>> fieldAccessor) {
        if(currentResource.isPresent()) {
            Field<F> field = fieldAccessor.apply(currentResource.get());
            return ofNullable(field.get());
        }
        return Optional.empty();
    }

    /**
     * @return The current resource in the traversal. May be null if the resource does not exist.
     */
    @Override
    public T get() {
        return currentResource.get();
    }

    /**
     * @return True, if the current resource in the traversal exists, false otherwise.
     */
    public boolean isPresent() {
        return currentResource.isPresent();
    }
}
