package com.redhat.pantheon.extension.url;

import com.redhat.pantheon.helper.PantheonConstants;
import com.redhat.pantheon.model.document.DocumentVariant;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Optional;

/**
 * Provides information on Customer Portal view url path.
 * This class provides the following url fragments for Customer Portal view url.
 *      URL_PRFIX,
 *      document type
 *      document locale
 *      customer portal host
 */
public abstract class CustomerPortalUrlProvider implements UrlProvider {

    protected static final String URL_PREFIX = "/documentation/";

    protected String getDocumentType(DocumentVariant variant) {
        return PantheonConstants.RESOURCE_TYPE_ASSEMBLYVARIANT.equals(variant.getResourceType()) ? "guide" : "topic";
    }

    protected String getLocale(DocumentVariant variant) {
        return variant.getParentLocale().getName();
    }

    public String getHost(ResourceResolver resolver) {
        Optional<Resource> conf = Optional.ofNullable(resolver.getResource("/conf/pantheon"));
        return conf.map(Resource::getValueMap)
                .map(m -> m.get("pant:portalUrl", String.class))
                .orElse("");
    }
}
