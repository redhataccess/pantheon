package com.redhat.pantheon.model;

import com.google.common.collect.ImmutableList;
import org.apache.sling.api.resource.ValueMap;

import java.util.List;

/**
 * A simple model to represent a query result page when running queries.
 */
public class QueryResultPage {

    private final ImmutableList<ValueMap> results;
    private final long offset;

    public QueryResultPage(List<ValueMap> results, long offset) {
        this.results = ImmutableList.copyOf(results);
        this.offset = offset;
    }

    public List<ValueMap> getResults() {
        return results;
    }

    public int getSize() {
        return results.size();
    }

    public long getNextOffset() {
        return offset + getSize();
    }

}
