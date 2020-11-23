package com.redhat.pantheon.model.api;

import org.jetbrains.annotations.Nullable;

public class NullObjects {

    private static final NullChild<?> NULL_CHILD = new NullChild<>();

    private static final Field<?> NULL_FIELD = new NullField<>();

    private NullObjects() {
    }

    public static final Child<?> nullChild() {
        return NULL_CHILD;
    }

    public static final Field<?> nullField() {
        return NULL_FIELD;
    }

    public static class NullChild<T extends SlingModel> implements Child<T> {
        private NullChild() {
        }

        @Override
        public T create() {
            throw new UnsupportedOperationException();
        }

        @Override
        public T get() {
            return null;
        }
    }

    public static class NullField<T> implements Field<T> {

        @Override
        public void set(@Nullable T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <R> Field<R> toFieldType(Class<R> newFieldType) {
            return (Field<R>) nullField();
        }

        @Override
        public T get() {
            return null;
        }
    }
}
