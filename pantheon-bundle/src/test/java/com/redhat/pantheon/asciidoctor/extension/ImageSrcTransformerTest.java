package com.redhat.pantheon.asciidoctor.extension;

import com.redhat.pantheon.conf.GlobalConfig;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.asciidoctor.ast.Document;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.util.Base64;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(SlingContextExtension.class)
class ImageSrcTransformerTest {

    final Pattern imageRegEx = Pattern.compile(GlobalConfig.IMAGE_PATH_PREFIX + "/.*");

    SlingContext sc = new SlingContext();

    @Test
    void process() {
        // Given
        final String html = "<html>" +
                "This is a sample html" +
                "<p>" +
                "with a single image " +
                "<img src='some/image/path'>" +
                "</p>" +
                "</html>";
        sc.build()
                .resource("/a/module")
                .resource("/a/some/image/path")
                .commit();
        ImageSrcTransformer transformer = new ImageSrcTransformer(sc.resourceResolver().getResource("/a/module"));

        // When
        String processedOutput = transformer.process(mock(Document.class), html);

        // Then
        assertNotEquals(html, processedOutput);
        org.jsoup.nodes.Document doc = Jsoup.parse(processedOutput, "UTF-8");
        assertFalse(doc.select("img").isEmpty());
        doc.select("img").forEach(image -> {
            assertTrue(image.attr("src").matches(imageRegEx.pattern()));
        });
    }

    @Test
    void encodeImgSrc() {
        // Given
        String path1 = "/a/content/image.png";
        String path2 = "../../a/relative/image.jpg";

        // When
        ImageSrcTransformer transformer = new ImageSrcTransformer(mock(Resource.class));

        // Then
        assertTrue(imageRegEx.matcher(transformer.encodeImgSrc(path1)).matches());
        assertTrue(imageRegEx.matcher(transformer.encodeImgSrc(path2)).matches());
    }
}
