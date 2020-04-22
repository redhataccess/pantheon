package com.redhat.pantheon.servlet.debug;

import com.redhat.pantheon.servlet.AbstractJsonQueryServlet;
import com.redhat.pantheon.servlet.ServletUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.jcr.query.Query;
import javax.servlet.Servlet;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Debug servlet which provides arbitrary query capability",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/debug/query.sql2.json")
public class Sql2QueryServlet extends AbstractJsonQueryServlet {

    @Override
    protected String getQueryLanguage() {
        return Query.JCR_SQL2;
    }

    @Override
    protected String getQuery(SlingHttpServletRequest request) {
        String query = ServletUtils.paramValue(request, "query");
        System.out.println("Query: " + query);
        return query;
    }
}
