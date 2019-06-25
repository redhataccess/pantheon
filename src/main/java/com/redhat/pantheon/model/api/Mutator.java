package com.redhat.pantheon.model.api;

interface Mutator<T> extends ResourceMember {

    void set(T t);
}
