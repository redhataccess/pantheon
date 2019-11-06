package com.redhat.pantheon.model;

import java.util.Map;

/**
 * A simple model to represent a single element result when running queries.
 */
public class QuerySingleResult {

    private final Map<String, Object> result;

    public QuerySingleResult(Map<String, Object> result) {
        this.result = result;
    }

    public Map<String, Object> getResult() {
        return result;
    }

}
