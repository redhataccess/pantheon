package com.redhat.pantheon.model.api;

import java.util.function.Supplier;

/**
 * An accessor member for a JCR property. Accessors are resource members themselves.
 *
 * @param <T> The type of property to access.
 */
interface Accessor<T> extends Supplier<T>, ResourceMember {
}
