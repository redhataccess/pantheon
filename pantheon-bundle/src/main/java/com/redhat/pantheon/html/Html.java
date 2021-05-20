package com.redhat.pantheon.html;

import com.redhat.pantheon.extension.url.UrlException;
import com.redhat.pantheon.extension.url.UrlProvider;
import com.redhat.pantheon.jcr.JcrQueryHelper;
import com.redhat.pantheon.model.document.DocumentVariant;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
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

    private static final String UUID_HREF_REGEX = "(?<uuid>[\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12})(?:\\.html?)?";
    private static final Pattern UUID_HREF_PATTERN = Pattern.compile(UUID_HREF_REGEX);

    private static final Logger log = LoggerFactory.getLogger(Html.class);

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

    public static Function<Document, Document> rewriteUuidUrls(ResourceResolver resolver, UrlProvider provider) {
        return document -> {
            document.select("a").stream()
                    .filter(link -> link.attributes().hasKey("href"))
                    .forEach(link -> {
                        String href = link.attributes().get("href");
                        Matcher m = UUID_HREF_PATTERN.matcher(href);
                        if (m.matches()) {
                            String uuid = m.group("uuid"); // uuid to a document variant
                            try {
                                Resource resource = resolver.getResource(resolver.adaptTo(Session.class).getNodeByIdentifier(uuid).getPath());
                                DocumentVariant variant = resource.adaptTo(DocumentVariant.class);
                                String url = provider.generateUrlString();
                                if (provider.getUrlType() == UrlProvider.urlType.LIVE) {
                                    link.attr("href", url);
                                }
                            } catch (RepositoryException | UrlException e) {
                                log.warn("Attempted to rewrite URL for link target " + uuid + " but was unsuccessful.", e);
                            }
                        }
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

    /**
     * An extractor function that returns the section of the document that matches the provided id.
     * If no element with that id is found, then the 'fallback' extractor is executed instead.
     * @param id
     * @param fallback
     * @return
     */
    public static Function<Document, String> getElementById(String id, Function<Document, String> fallback) {
        return document -> Optional.ofNullable(document.getElementById(id)).map(Element::toString).orElse(fallback.apply(document));
    }

    /**
     * An extractor function that returns the section of the document containing the specified tag name.
     * If no element with that name is found, then the 'fallback' extractor is executed instead.
     * @param tagName
     * @param fallback
     * @return
     */
    public static Function<Document, String> getElementByTagName(String tagName, Function<Document, String> fallback) {
        return document -> Optional.ofNullable(document.getElementsByTag(tagName)).map(Elements::first).map(Element::toString).orElse(fallback.apply(document));
    }
}
