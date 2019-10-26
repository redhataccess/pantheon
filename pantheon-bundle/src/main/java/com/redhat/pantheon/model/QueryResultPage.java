package com.redhat.pantheon.model;

import com.google.common.collect.ImmutableList;
import org.apache.sling.api.resource.ValueMap;

import java.util.List;
import java.util.Map;

/**
 * A simple model to represent a query result page when running queries.
 */
public class QueryResultPage {

    private final ImmutableList<Map<String, Object>> results;
    private final long offset;
    private final boolean hasNextPage;

    public QueryResultPage(List<Map<String, Object>> results, long offset, boolean hasNextPage) {
        this.results = ImmutableList.copyOf(results);
        this.offset = offset;
        this.hasNextPage = hasNextPage;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }

    public int getSize() {
        return results.size();
    }

    public long getNextOffset() {
        return offset + getSize();
    }

    public boolean getHasNextPage() {
        return hasNextPage;
    }

}
