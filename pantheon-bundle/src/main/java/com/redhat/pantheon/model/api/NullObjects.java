package com.redhat.pantheon.model.api;

import org.jetbrains.annotations.Nullable;

/**
 * A collection of mostly singleton null-object classes to facilitate navigation and
 * other null-sensitive operations. These are mostly implementations of the model
 * interfaces in this package.
 */
class NullObjects {

    private static final NullChild<?> NULL_CHILD = new NullChild<>();

    private static final Field<?> NULL_FIELD = new NullField<>();

    private NullObjects() {
    }

    /**
     * Obtain a null {@link Child} object.
     */
    public static final Child<?> nullChild() {
        return NULL_CHILD;
    }

    /**
     * Obtain a null {@link Field} object.
     */
    public static final Field<?> nullField() {
        return NULL_FIELD;
    }

    /**
     * Null implementation of the {@link Child} interface
     * @see NullObjects#nullChild() for a way to use the singleton instance
     */
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

    /**
     * Null implementation of the {@link Field} interface
     * @see NullObjects#nullField() for a way to use the singleton instance
     */
    public static class NullField<T> implements Field<T> {

        private NullField() {
        }

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
