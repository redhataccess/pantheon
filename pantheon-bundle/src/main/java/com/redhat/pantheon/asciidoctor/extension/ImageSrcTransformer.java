package com.redhat.pantheon.asciidoctor.extension;

import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;
import org.jsoup.Jsoup;

import java.util.Base64;

import static com.redhat.pantheon.conf.GlobalConfig.IMAGE_PATH_PREFIX;

/**
 * An asciidoctor post processor which transforms all image locations in the resulting html to an encoded and absolutely
 * resolving path comprised of a prefix and a unique identifier for the image. This unique identifier consists of the
 * Base64 encoding of the actual absolute path of the image resource in the JCR repository.
 *
 * @see com.redhat.pantheon.servlet.assets.ImageServletFilter for the servlet serving these paths
 * @author Carlos Munoz
 */
public class ImageSrcTransformer extends Postprocessor {

    private final Resource module;

    public ImageSrcTransformer(Resource module) {
        this.module = module;
    }

    @Override
    public String process(Document document, String output) {
        org.jsoup.nodes.Document doc = Jsoup.parse(output, "UTF-8");

        ResourceResolver resourceResolver = module.getResourceResolver();

        // add a prefix to all images
        doc.select("img")
                .forEach(imageElement -> {
                    String imgSrc = imageElement.attr("src");
                    String imagePath = PathUtils.concat(module.getParent().getPath(), imgSrc);
                    imageElement.attr("src", encodeImgSrc(imagePath));
                });
        output = doc.html();

        return output;
    }

    String encodeImgSrc(String imageResourcePath) {
        return IMAGE_PATH_PREFIX + "/" + Base64.getUrlEncoder().encodeToString(imageResourcePath.getBytes());
    }
}
