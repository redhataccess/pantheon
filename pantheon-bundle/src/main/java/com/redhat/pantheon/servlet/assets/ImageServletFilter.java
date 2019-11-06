package com.redhat.pantheon.servlet.assets;

import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;

import static com.redhat.pantheon.conf.GlobalConfig.IMAGE_PATH_PREFIX;

/**
 * A servlet filter which serves image assets only. Images must be referened using an absolute path comprised
 * of a prefix (see @{link IMAGE_PATH_PREFIX} for the exact value) plus an identifier. In this servlet the identifier is
 * a Base64 encoding of the actual path stored in the JCR repository for the image.
 * </br>
 * </br>
 * So for example, an image path might be:
 * http://my.server/imageassets/L2NvbnRlbnQvcmVwb3NpdG9yaWVzL3BhbnRoZW9uU2FtcGxlUmVwby9pbWFnZXMvdGlnZXIucG5n
 * with /imageassets being the prefix and everything after (exluding the separating slash) being a Base64 encoding of the
 * path: /content/repositories/pantheonSampleRepo/images/tiger.png which resolves absolutely and unambigously to an image
 * in the system.
 *
 * @author Carlos Munoz
 */
@Component(
        service = Filter.class
)
@SlingServletFilter(
        methods = "GET",
        pattern = "/imageassets/.*")
public class ImageServletFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Get the id, everything after the prefix
        String contextPath = ((HttpServletRequest)request).getPathInfo();
        contextPath = contextPath.substring((IMAGE_PATH_PREFIX + "/").length());

        String imagePath = new String(Base64.getUrlDecoder().decode(contextPath));
        request.getRequestDispatcher(imagePath)
            .forward(request, response);
    }

    @Override
    public void destroy() {
    }
}
