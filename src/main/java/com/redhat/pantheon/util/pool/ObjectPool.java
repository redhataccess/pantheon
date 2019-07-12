package com.redhat.pantheon.util.pool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Collections.synchronizedSet;

/**
 * A simple object pool to manage objects which are expensive to create.
 * The pool accepts an initial and maximum size. The initial size determines how many
 * object instances will be available at pool creation time.
 * @param <T> The type of objects managed by the pool.
 * @author Carlos Munoz
 */
public class ObjectPool<T> implements AutoCloseable {

    private final ConcurrentLinkedQueue<T> available = new ConcurrentLinkedQueue<>();

    private final Set<T> borrowed = synchronizedSet(new HashSet<>());

    private final PooledObjectLifecycle<T> objectLifecycle;

    private final int initialSize;

    private final int maxSize;

    /**
     * Default constructor
     * @param objectLifecycle A lifecycle object that determines how objects are created and disposed
     * @param initialSize The initial size of the pool. This determines how many active objects are ready to be
     *                    borrowed after the pool is created.
     * @param maxSize The maximum number of objects this pool can handle.
     */
    public ObjectPool(PooledObjectLifecycle<T> objectLifecycle, int initialSize, int maxSize) {
        this.objectLifecycle = objectLifecycle;
        this.initialSize = initialSize;
        this.maxSize = maxSize;
        populateToInitialSize();
    }

    private void populateToInitialSize() {
        while (available.size() < getInitialSize()){
            T obj = objectLifecycle.createInstance();
            available.offer(obj);
        }
    }

    public int getInitialSize() {
        return initialSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Takes an object from the pool.
     * @return An object which is ready to use.
     * @throws RuntimeException If the pool is empty and the maxium size has been reached. i.e. all
     * objects have been borrowed.
     */
    public T borrowObject() {
        T borrowedObj = available.poll();

        if (borrowedObj == null) {
            synchronized (available) {
                if (getSize() < getMaxSize()) {
                    borrowedObj = objectLifecycle.createInstance();
                } else {
                    // ??? What to do if the queue is full?
                    throw new RuntimeException("Object pool is empty. No objects to offer.");
                }
            }
        }

        borrowed.add(borrowedObj);
        return borrowedObj;
    }

    /**
     * Return an object to the pool once it has been used.
     * @param obj The object to return to the pool
     * @throws RuntimeException If the returned object is not managed by this pool.
     */
    public void returnObject(T obj) {
        if(!borrowed.contains(obj)) {
            throw new RuntimeException("Attempted to return an object which is not managed by this pool");
        }

        synchronized (this) {
            borrowed.remove(obj);
            available.offer(obj);
        }
    }

    /**
     * The number of objects currently borrowed from the pool
     */
    public int getBorrowedCount() {
        return borrowed.size();
    }

    /**
     * The number of objects available to be borrowed from the pool.
     */
    public int getAvailableCount() {
        return available.size();
    }

    /**
     * The current size of the pool. This includes borrowed and available objects.
     */
    public int getSize() {
        return getBorrowedCount() + getAvailableCount();
    }

    /**
     * Removes all objects from the pool. Already borrowed objects may not be returned to the
     * pool after calling the clear method.
     */
    public synchronized void clear() {
        available.forEach(t -> objectLifecycle.destroy(t));
        available.clear();
        borrowed.clear();
    }

    /**
     * Closes the pool
     * @throws Exception
     */
    @Override
    public synchronized void close() throws Exception {
        clear();
    }
}
