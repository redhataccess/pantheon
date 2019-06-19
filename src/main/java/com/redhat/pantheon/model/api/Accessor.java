package com.redhat.pantheon.model.api;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * An accessor member for a JCR property. Accessors are resource members themselves.
 *
 * @param <T> The type of property to access.
 */
interface Accessor<T> extends Supplier<T>, ResourceMember {

    default T orElse(@Nonnull T defaultVal) {
        T val = get();
        return val == null ? defaultVal : val;
    }
}
