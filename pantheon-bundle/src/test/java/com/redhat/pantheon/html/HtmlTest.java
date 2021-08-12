package com.redhat.pantheon.html;

import com.google.common.base.Charsets;
import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.extension.url.CustomerPortalUrlUuidProvider;
import com.redhat.pantheon.extension.url.UrlProvider;
import com.redhat.pantheon.model.document.DocumentVariant;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Carlos Munoz
 */
class HtmlTest {

    final Pattern imageRegEx = Pattern.compile(GlobalConfig.IMAGE_PATH_PREFIX + "/.*");
    SlingContext sCtx = new SlingContext(ResourceResolverType.JCR_OAK);

    @Test
    void parse() {
        // Given
        String html1 = "<html>Simple html</html>";
        String html2 = "<html><b>Broken html</html>";

        // When
        Document doc1 = Html
                .parse(Charsets.UTF_8.name())
                .apply(html1);
        Document doc2 = Html
                .parse(Charsets.UTF_8.name())
                .apply(html2);

        // Then
        assertNotNull(doc1);
        assertNotNull(doc2);
    }

    @Test
    void encodeAllImageLocations() {
        // Given
        Resource resource = mock(Resource.class);
        Resource parent = mock(Resource.class);
        when(resource.getParent()).thenReturn(parent);
        when(parent.getPath()).thenReturn("/parent/path");
        String html = "<html><img src=\"an/image/location.png\"></html>";

        // When
        String transformedHtml = Html.parse(Charsets.UTF_8.name())
                .andThen(Html.encodeAllImageLocations(resource))
                .andThen(doc -> doc.toString())
                .apply(html);

        // Then
        Document doc = Jsoup.parse(transformedHtml, "UTF-8");
        assertFalse(doc.select("img").isEmpty());
        doc.select("img").forEach(image -> {
            assertTrue(image.attr("src").matches(imageRegEx.pattern()));
        });
    }

    @Test
    void rewriteUuidUrls() {
        // Given
        sCtx.create().resource("/test",
                "name", "a-name",
                "jcr:primaryType", "pant:module");
        sCtx.create().resource("/test/child",
                "name", "child-name");
        String resourceUuid = sCtx.resourceResolver()
                .getResource("/test")
                .getValueMap()
                .get("jcr:uuid")
                .toString();

        String html = "<html>" +
                "<head><title>This is the head</title></head>" +
                "<body>This is the body" +
                "<a href='1234'>vanilla hyperlink</a>" +
                "<a href='" + resourceUuid + "'>link with a valid uuid</a>" +
                "<a href='" + resourceUuid + ".htm'>link with a valid suffix</a>" +
                "<a href='" + resourceUuid + ".html'>link with a valid suffix</a>" +
                "<a href='" + resourceUuid + ".xml'>link with an invalid suffix</a>" +
                "</body>" +
                "</html>";

        // When
        String transformedHtml = Html.parse(Charsets.UTF_8.name())
                .andThen(Html.rewriteUuidUrls(sCtx.resourceResolver(), new UrlProvider(null) {

                    @Override
                    public String generateUrlString() {
                        return "someTestString";
                    }

                    @Override
                    public urlType getUrlType() {
                        return urlType.LIVE;
                    }
                }))
                .andThen(doc -> doc.toString())
                .apply(html);

        // Then
        Document doc = Jsoup.parse(transformedHtml, "UTF-8");
        List<Element> elms = doc.select("a").stream().collect(Collectors.toList());
        assertFalse(elms.isEmpty());
        assertTrue("1234".equals(elms.get(0).attr("href")));
        assertTrue("someTestString".equals(elms.get(1).attr("href")));
        assertTrue("someTestString".equals(elms.get(2).attr("href")));
        assertTrue("someTestString".equals(elms.get(3).attr("href")));
        assertTrue((resourceUuid + ".xml").equals(elms.get(4).attr("href")));
    }

    @Test
    void getBody() {
        // Given
        String html = "<html>" +
                "<head><title>This is the head</title></head>" +
                "<body>This is the body</body>" +
                "</html>";

        // When
        String body = Html.parse(Charsets.UTF_8.name())
                .andThen(Html.getBody())
                .apply(html);

        // Then
        assertEquals("This is the body", body);
    }
}