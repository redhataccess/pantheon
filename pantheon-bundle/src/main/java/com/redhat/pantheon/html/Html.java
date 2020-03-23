package com.redhat.pantheon.html;

import com.redhat.pantheon.jcr.JcrQueryHelper;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.jcr.RepositoryException;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.redhat.pantheon.conf.GlobalConfig.IMAGE_PATH_PREFIX;

/**
 * An html manipulation and inspection class. Contains a set of generic functional constructs to
 * parse and manipulate html conent (using {@link Jsoup}) as well as some commonly used transformations.
 *
 * @author Carlos Munoz
 */
public class Html {

    private static final Pattern UUID_PATTERN = Pattern.compile("([\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12})");

    private Html() {
    }

    /**
     * Creates an html parser function which can then be chained to manipulate or extract content.
     * @param charsetName The character set to use when parsing the html
     * @return A chainable function for parsing and manipulating html
     */
    public static Function<String, Document> parse(final String charsetName) {
        return (htmlContent) -> Jsoup.parse(htmlContent, charsetName);
    }

    /**
     * A transformer function which encodes all image locations in the html to use a generic format.
     * @param module The root module which is including the image.
     * @return An html transformer function.
     */
    public static Function<Document, Document> encodeAllImageLocations(final Resource module) {
        return document -> {
            document.select("img")
                    .forEach(imageElement -> {
                        String imgSrc = imageElement.attr("src");
                        String imagePath = PathUtils.concat(module.getParent().getPath(), imgSrc);
                        imageElement.attr("src",
                                IMAGE_PATH_PREFIX + "/" + Base64.getUrlEncoder().encodeToString(imagePath.getBytes()));
                    });
            return document;
        };
    }

    /**
     * An extractor function which returns just the body content for the parsed html document.
     * @return An html extactor function.
     */
    public static Function<Document, String> getBody() {
        // returns the inner content of the body tag
        return document -> document.body().html();
    }
}
