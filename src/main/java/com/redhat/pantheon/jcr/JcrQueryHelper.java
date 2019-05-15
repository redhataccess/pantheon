package com.redhat.pantheon.jcr;

import com.google.common.collect.Lists;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Provides utility methods for querying the Sling JCR repository.
 */
public class JcrQueryHelper {

    private final ResourceResolver resourceResolver;

    public JcrQueryHelper(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    /**
     * A convenience method to run a query against the JCR Repository adding limit and offset values useful for
     * pagination.
     * @param query A full JCR SQL2 query
     * @param limit the number of results to return (for pagination)
     * @param offset i.e. How many results to skip (for pagination)
     * @return
     * @throws RepositoryException
     */
    public Stream<Resource> query(String query, long limit, long offset)
            throws RepositoryException {
        Session session = resourceResolver.adaptTo(Session.class);

        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query queryObj = queryManager.createQuery(query, Query.JCR_SQL2);
        queryObj.setLimit(limit);
        queryObj.setOffset(offset);
        QueryResult result = queryObj.execute();

        // Transform to sling resources
        // TODO This might be a costly transformation if done on large result sets
        return Lists.newArrayList((Iterator<Row>) result.getRows())
                .stream()
                .map(row -> {
                    try {
                        return resourceResolver.getResource(row.getPath());
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Stream<Resource> query(String query)
            throws RepositoryException {
        return query(query, -1, 0);
    }

}
