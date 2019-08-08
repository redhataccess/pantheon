package com.redhat.pantheon.model.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * A simple annotation to denote the JCR primary type of a {@link com.redhat.pantheon.model.api.SlingResource}
 * class.
 */
public @interface JcrPrimaryType {
    String value();
}
