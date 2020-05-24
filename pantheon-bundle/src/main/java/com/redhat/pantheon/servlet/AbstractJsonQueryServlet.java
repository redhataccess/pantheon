package com.redhat.pantheon.servlet;

import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.QueryResultPage;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.newHashMap;
import static com.redhat.pantheon.servlet.ServletUtils.paramValueAsLong;
import static com.redhat.pantheon.servlet.ServletUtils.writeAsJson;

/**
 * A base servlet that helps to create simple Json results from custom queries.
 * This servlet should be extended to create components that render lists of
 * results in json format. See methods to be overriden for details on how to
 * customize sub classes.
 *
 * @author Carlos Munoz
 */
public abstract class AbstractJsonQueryServlet extends SlingSafeMethodsServlet {

    private static final long RESULT_SIZE_LIMIT = 1000;

    /** Parameter name for the maximum size of the results */
    protected static final String PARAM_LIMIT = "limit";

    /** Parameter name for the start offset of the result set */
    protected static final String PARAM_OFFSET = "offset";

    /**
     * Returns the query language to use when executing the query.
     * @return The query language to use. This defaults to JCR-SQL2.
     */
    protected String getQueryLanguage() {
        return Query.JCR_SQL2;
    }

    /**
     * Returns the query to execute. The query may be modified depending on the provided
     * parameters in the request.
     * @param request The sling servlet request
     * @return A string with the query to execute. The results of this query will be used
     * to render the list of results.
     */
    protected abstract String getQuery(SlingHttpServletRequest request);

    /**
     * Provides a way to modify the returned objects based on the found resources.
     * The default implementation just returns the corresponding value map.
     * @param resource The Resource obtained as a result of the query.
     * @return A map with the actual value to be returned to the servlet's caller.
     */
    protected Map<String, Object> resourceToMap(Resource resource) {
        return newHashMap(resource.getValueMap());
    }

    @Override
    protected final void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response)
            throws ServletException, IOException {

        // default parameters
        long limit = paramValueAsLong(request, PARAM_LIMIT, RESULT_SIZE_LIMIT);
        long offset = paramValueAsLong(request, PARAM_OFFSET, 0L);

        JcrQueryHelper queryHelper = new JcrQueryHelper(request.getResourceResolver());
        try {
            // do limit+1 to know if there is a next page
            Stream<Resource> results = queryHelper.query(getQuery(request), limit+1, offset, getQueryLanguage());
            List<Map<String, Object>> resultList = results.map(this::resourceToMap)
                    .collect(Collectors.toList());
            boolean hasNextPage = false;
            if(resultList.size() > limit) {
                resultList.remove(resultList.size() - 1); // Removing the +1 element that we added for the next page
                hasNextPage = true;
            }
            QueryResultPage resultPage = new QueryResultPage(
                    resultList,
                    offset,
                    hasNextPage);

            writeAsJson(response, resultPage);
        } catch (RepositoryException e) {
            throw new ServletException(e);
        }
    }

}
