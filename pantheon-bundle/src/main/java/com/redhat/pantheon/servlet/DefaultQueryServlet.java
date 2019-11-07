package com.redhat.pantheon.servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;

import static com.redhat.pantheon.servlet.ServletUtils.paramValue;

/**
 * This query servlet applies to any resource in the content tree. It enables an action under the resource
 * to search for nodes. It will search for all nodes under the requested resource, not just direct descendants.
 * Currently the only parameter being used is the node name.
 * <p>
 * This servlet is enabled by default for the 'query' selector and 'json' extension.
 * </p>
 * <p>
 * Child nodes can now be searched at the following endpoint (assuming the node location is /content/mynode):
 * </p>
 * <p>
 * GET /content/mynode.query.json?...
 * <p>
 * </p>
 * Available query parameters for this servlet are:
 * <br>
 * nodeType (optional): The JCR node type to look for. Default value: 'nt:base'
 * <br>
 * where (optional): Any additional JCR-QL2 where statements to further filter the results.
 * e.g. '[app:my-property] = "prop value"'. Default value: '' (no additional filtering will be performed)
 * <br>
 * orderBy (optional): A JCR-QL2 order by statement to sort the results. e.g. '[app:my-property] desc'.
 * Default value: '' (no sorting will be performed)
 * <br>
 * limit (optional): The maximum number of results to return. Default value: 1000
 * <br>
 * offset (optional): The number of results to skip (for pagination). Default value: 0
 * <p>
 * </p>
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=sling/servlet/default",
                "sling.servlet.selectors=query",
                "sling.servlet.extensions=json",
                Constants.SERVICE_DESCRIPTION + "=Utility servlet capable of running JCR queries",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
public class DefaultQueryServlet extends AbstractJsonQueryServlet {
    private final Logger logger = LoggerFactory.getLogger(DefaultQueryServlet.class);

    private static final long RESULT_SIZE_LIMIT = 1000;

    @Override
    protected String getQuery(SlingHttpServletRequest request) {
        // Get the query parameter(s)
        String nodeType = paramValue(request, "nodeType","nt:base");
        String where = paramValue(request, "where");
        String orderBy = paramValue(request, "orderBy");

        StringBuilder query = new StringBuilder("select * from ")
                .append("[").append(nodeType).append("]")
                .append(" as node ")
                .append(" where ISDESCENDANTNODE(\"" + request.getResource().getPath() + "\") ");

        if (where != null) {
            query.append(" AND (").append(where).append(") ");
        }
        if (orderBy != null && !orderBy.isEmpty()) {
            query.append(" order by ").append(orderBy);
        }

        return query.toString();
    }
}
