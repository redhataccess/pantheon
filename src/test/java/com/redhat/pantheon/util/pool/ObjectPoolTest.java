package com.redhat.pantheon.util.pool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
class ObjectPoolTest {

    @Mock(answer = RETURNS_MOCKS)
    PooledObjectLifecycle<Object> lifecycle;

    @Test
    void initialState() {
        // Given

        // When
        ObjectPool<?> pool = new ObjectPool<>(lifecycle, 3, 5);

        // then
        assertEquals(3, pool.getSize());
        assertEquals(5, pool.getMaxSize());
    }

    @Test
    void borrowObject() {
        // Given
        ObjectPool<?> pool = new ObjectPool<>(lifecycle, 3, 5);

        // When
        Object o = pool.borrowObject();

        // then
        assertNotNull(o);
        assertEquals(3, pool.getSize());
        assertEquals(5, pool.getMaxSize());
        assertEquals(2, pool.getAvailableCount());
        assertEquals(1, pool.getBorrowedCount());
    }

    @Test
    void borrowObjectWhenNotCreated() {
        // Given
        ObjectPool<?> pool = new ObjectPool<>(lifecycle, 0, 5);

        // When
        Object o = pool.borrowObject();

        // then
        assertNotNull(o);
        verify(lifecycle, times(1)).createInstance();
        assertEquals(1, pool.getSize());
        assertEquals(5, pool.getMaxSize());
        assertEquals(0, pool.getAvailableCount());
        assertEquals(1, pool.getBorrowedCount());
    }

    @Test
    void borrowObjectWhenPoolIsFull() {
        // Given
        ObjectPool<?> pool = new ObjectPool<>(lifecycle, 1, 1);
        pool.borrowObject(); // use the only instance available

        // When

        // then
        assertThrows(Exception.class, pool::borrowObject);
        assertEquals(1, pool.getSize());
        assertEquals(1, pool.getMaxSize());
        assertEquals(0, pool.getAvailableCount());
        assertEquals(1, pool.getBorrowedCount());
    }

    @Test
    void returnObject() {
        // Given
        ObjectPool<Object> pool = new ObjectPool<>(lifecycle, 3, 5);

        // When
        Object o = pool.borrowObject();
        pool.returnObject(o);

        // then
        assertNotNull(o);
        assertEquals(3, pool.getSize());
        assertEquals(5, pool.getMaxSize());
        assertEquals(3, pool.getAvailableCount());
        assertEquals(0, pool.getBorrowedCount());
    }

    @Test
    void returnUnmanagedObject() {
        // Given
        ObjectPool<Object> pool = new ObjectPool<>(lifecycle, 0, 1);

        // When
        Object unmanaged = new Object();

        // then
        assertThrows(Exception.class, () -> pool.returnObject(unmanaged));
        assertEquals(0, pool.getSize());
        assertEquals(1, pool.getMaxSize());
        assertEquals(0, pool.getAvailableCount());
        assertEquals(0, pool.getBorrowedCount());
    }

    @Test
    void getUsedCount() {
    }

    @Test
    void clear() {
        // Given
        ObjectPool<Object> pool = new ObjectPool<>(lifecycle, 3, 5);

        // When
        pool.clear();

        // then
        assertEquals(0, pool.getAvailableCount());
        assertEquals(0, pool.getBorrowedCount());
    }

    @Test
    void close() throws Exception {
        // Given
        ObjectPool<Object> pool = new ObjectPool<>(lifecycle, 3, 5);

        // When
        pool.close();

        // then
        assertEquals(0, pool.getAvailableCount());
        assertEquals(0, pool.getBorrowedCount());
    }
}