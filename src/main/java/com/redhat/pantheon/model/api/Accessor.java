package com.redhat.pantheon.model.api;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

interface Accessor<T> extends Supplier<T>, ResourceMember {

    default T orElse(@Nonnull T defaultVal) {
        T val = get();
        return val == null ? defaultVal : val;
    }
}
