package com.redhat.pantheon.model.api;

@FunctionalInterface
interface Mutator<T> extends ResourceMember {

    void set(T t);
}
