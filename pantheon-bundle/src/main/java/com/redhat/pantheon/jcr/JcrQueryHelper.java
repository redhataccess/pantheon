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
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
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
     * A convenience method to run a query in any supported language against the JCR Repository,
     * adding limit and offset values useful for pagination.
     * @param query A full query in any supported query language. See {@link Query} for available languages.
     * @param limit the number of results to return (for pagination)
     * @param offset i.e. How many results to skip (for pagination)
     * @param queryLanguage The query language to use.
     * @return A stream of sling resources resulting from the query
     * @throws RepositoryException
     */
    public Stream<Resource> query(String query, long limit, long offset, String queryLanguage)
            throws RepositoryException {
        Session session = resourceResolver.adaptTo(Session.class);

        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query queryObj = queryManager.createQuery(query, queryLanguage);
        queryObj.setLimit(limit);
        queryObj.setOffset(offset);
        QueryResult result = queryObj.execute();

        // Transform to sling resources
        return transform(result);
    }

    /**
     * A convenience method to run a JCR SQL2 query against the JCR Repository adding limit and offset values useful for
     * pagination.
     * @param query A full JCR SQL2 query
     * @param limit the number of results to return (for pagination)
     * @param offset i.e. How many results to skip (for pagination)
     * @return A stream of sling resources resulting from the query
     * @throws RepositoryException
     */
    public Stream<Resource> query(String query, long limit, long offset)
            throws RepositoryException {
        return query(query, limit, offset, Query.JCR_SQL2);
    }

    /**
     * A convenience method to run a query against the JCR Repository adding limit and offset values useful for
     * pagination.
     * @param query A full JCR SQL2 query
     * @return A stream of sling resources resulting from the query
     * @throws RepositoryException
     */
    public Stream<Resource> query(String query)
            throws RepositoryException {
        return query(query, Long.MAX_VALUE, 0);
    }

    /**
     * Queries all resources for a given JCR node type
     * @param nodeType The node type to query for
     * @param limit
     * @param offset
     * @return
     * @throws RepositoryException
     */
    public Stream<Resource> queryAll(String nodeType, long limit, long offset) throws RepositoryException {
        QueryObjectModelFactory qomFactory = getQueryObjectModelFactory();
        Selector selector = qomFactory.selector(nodeType, "r");
        // Default ordering by node path
        Ordering sortByPath = qomFactory.ascending(qomFactory.propertyValue("r", "jcr:path"));
        QueryObjectModel query = qomFactory.createQuery(selector, null, new Ordering[]{sortByPath}, null);
        query.setLimit(limit);
        query.setOffset(offset);
        QueryResult result = query.execute();
        return transform(result);
    }

    public Stream<Resource> queryAll(String nodeType) throws RepositoryException {
        return queryAll(nodeType, Long.MAX_VALUE, 0);
    }

    public QueryObjectModelFactory getQueryObjectModelFactory() throws RepositoryException {
        Session session = resourceResolver.adaptTo(Session.class);
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        return queryManager.getQOMFactory();
    }

    /**
     * Transforms a JCR query result into a stream of Sling resources
     * @param result the result to transform
     * @return A stream of sling resources
     * @throws RepositoryException
     */
    private Stream<Resource> transform(QueryResult result) throws RepositoryException {
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


}
