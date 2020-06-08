package com.redhat.pantheon.servlet.assets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.jetbrains.annotations.NotNull;
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
import java.util.regex.Matcher;

import static com.redhat.pantheon.conf.GlobalConfig.IMAGE_PATH_PREFIX;
import static com.redhat.pantheon.servlet.ServletUtils.getPathMatcher;

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
        pattern = ImageServletFilter.PATH_PATTERN)
public class ImageServletFilter implements Filter {

    static final String PATH_PATTERN = IMAGE_PATH_PREFIX + "/(?<assetId>.*)";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Get the id, everything after the prefix
        Matcher pathMatcher = getPathMatcher(PATH_PATTERN, (HttpServletRequest) request);
        String assetId = pathMatcher.group("assetId");

        String imagePath = new String(Base64.getUrlDecoder().decode(assetId));
        StringBuilder realPath = new StringBuilder(imagePath);

        //Find out if this resolves by itself already.
        ResourceResolver resolver = ((SlingHttpServletRequest) request).getResourceResolver();
        Resource res = resolver.resolve(imagePath);
        if (res == null || "sling:nonexisting".equals(res.getResourceType())) {
            //Because of symlinks, we need to resolve imagePath manually.
            String[] parts = imagePath.split("/");
            realPath.setLength(0);
            for (String part : parts) {
                if (part.isEmpty()) {
                    continue;
                }
                realPath.append("/").append(part);
                Resource resource = resolver.resolve(realPath.toString());
                if ("pantheon/symlink".equals(resource.getResourceType())) {
                    realPath.setLength(realPath.length() - part.length());
                    realPath.append(resource.getValueMap().get("pant:target", String.class));
                }
            }
        }

        request.getRequestDispatcher(realPath.toString().replaceAll("/+", "/"))
            .forward(request, response);
    }

    @Override
    public void destroy() {
    }
}
