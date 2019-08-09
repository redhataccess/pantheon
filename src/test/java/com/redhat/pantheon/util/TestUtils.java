package com.redhat.pantheon.util;

import com.google.common.base.Function;
import com.redhat.pantheon.model.api.SlingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A set of test-only utilities
 */
public class TestUtils {

    private TestUtils() {
    }

    /**
     * Registers a mock adapter on the sling context for the given model class. This way
     * {@link Resource#adaptTo(Class)} calls are handled properly in the test code.
     *
     * @param adapterClass The adapter class to register.
     * @param context The sling context to register the mock adapter
     * @param <T> A subtype of {@link SlingResource}
     */
    public static <T extends SlingResource> void registerMockAdapter(final Class<T> adapterClass,
                                                                     final SlingContext context) {
        Function<Resource, T> adapterFunction = resource -> {
            try {
                Constructor<T> constructor = adapterClass.getConstructor(Resource.class);
                return constructor.newInstance(resource);
            } catch (NoSuchMethodException | InstantiationException
                    | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };

        context.registerAdapter(Resource.class, adapterClass, adapterFunction);
    }
}
