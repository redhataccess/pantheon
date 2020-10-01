package com.redhat.pantheon.extension.url;

import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.document.DocumentMetadata;
import com.redhat.pantheon.model.document.DocumentVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Provides Customer Portal url with uuid.
 * This class generates a Customer Portal url with a UUID suffix.
 *
 */
public class CustomerPortalUrlUuidProvider extends CustomerPortalUrlProvider {

    private final Logger log = LoggerFactory.getLogger(CustomerPortalUrlUuidProvider.class);

    /**
     * Generates a customer portal URL path in the form of:
     * https://access.redhat.com/documentation/en-us/topic/rhel/8.3/aa77a8c8-f41e-4658-bc1d-993d8b22972a
     * @param variant
     * @return a URL path, if one is able to be generated, otherwise null
     */
    @Override
    public String generateUrlString(DocumentVariant variant) {
        try {
            DocumentMetadata metadata = variant.released().get().metadata().get();
            ProductVersion pv = metadata.productVersion().getReference();
            StringBuilder sb = new StringBuilder(getHost(variant.getResourceResolver()));
            sb.append(URL_PREFIX)
                    .append(getLocale(variant)).append("/")
                    .append(pv.getProduct().urlFragment().get()).append("/")
                    .append(pv.urlFragment().get()).append("/")
                    .append(getDocumentType(variant)).append("/")
                    .append(variant.uuid().get());
            return sb.toString();
        } catch (RepositoryException | NullPointerException e) {
            log.info("Could not construct URL path for " + variant.getPath(), e);
            return null;
        }
    }
}
