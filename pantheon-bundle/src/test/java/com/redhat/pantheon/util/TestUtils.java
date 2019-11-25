package com.redhat.pantheon.util;

import com.google.common.base.Function;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.SlingModels;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;

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
     * @param <T> A subtype of {@link SlingModel}
     */
    public static <T extends SlingModel> void registerMockAdapter(final Class<T> adapterClass,
                                                                  final SlingContext context) {
        Function<Resource, T> adapterFunction = resource -> SlingModels.getModel(resource, adapterClass);
        context.registerAdapter(Resource.class, adapterClass, adapterFunction);
    }

    public static void setReferenceValue(final Resource resource, final String refName, final Resource referenceable) {
        resource.adaptTo(ModifiableValueMap.class)
                .put(refName,
                        referenceable.getValueMap().get(JcrConstants.JCR_UUID));
    }
}
