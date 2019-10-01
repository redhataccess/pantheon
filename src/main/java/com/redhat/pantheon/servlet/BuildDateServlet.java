package com.redhat.pantheon.servlet;

import com.redhat.pantheon.use.PlatformData;
import org.apache.http.HttpStatus;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.redhat.pantheon.servlet.ServletUtils.writeAsJson;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet to fetch the current build date in the Admin Panel",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(value = "/pantheon/builddate.json")
public class BuildDateServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(BuildDateServlet.class);

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        try {
            String buildDate = getDate();
            Map<String, Object> currentBuildDate = new HashMap<>();
            currentBuildDate.put("buildDate",buildDate);
            currentBuildDate.put("commitHash", getCommitHash());
            writeAsJson(response, currentBuildDate);
        } catch (Exception e) {
            log.error("/builddate.json error", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected String getDate(){
        return PlatformData.getJarBuildDate();
    }

    protected String getCommitHash(){
        String commitHash;
        if (System.getenv("COMMIT_HASH") != null){
            commitHash = System.getenv("COMMIT_HASH");
        } else {
            commitHash = "COMMIT_HASH is not set, this might not be an OpenShift environment.";
        }
        return commitHash;
    }
}
