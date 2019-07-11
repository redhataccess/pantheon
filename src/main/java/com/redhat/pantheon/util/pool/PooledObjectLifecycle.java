package com.redhat.pantheon.util.pool;

/**
 * Determines the lifecycle of a pooled object.
 * @param <T>
 */
public interface PooledObjectLifecycle<T> {

    /**
     * Create an instance of an expensive object which needs to be pooled
     * @return a new instance of an object to be help in a pool
     */
    T createInstance();

    /**
     * Releases any necessary resources as part of destroying pooled objects.
     * @param obj The object to be destroyed and possibly removed from the pool
     */
    void destroy(T obj);
}
