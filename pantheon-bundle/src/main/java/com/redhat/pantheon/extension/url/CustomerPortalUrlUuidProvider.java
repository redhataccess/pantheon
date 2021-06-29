package com.redhat.pantheon.extension.url;

import com.redhat.pantheon.model.ProductVersion;
import com.redhat.pantheon.model.document.DocumentMetadata;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.servlet.ServletUtils;
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

    public CustomerPortalUrlUuidProvider(DocumentVariant documentVariant) {
        super(documentVariant);
    }

    /**
     * Generates a customer portal URL path in the form of:
     * https://access.redhat.com/documentation/en-us/topic/rhel/8.3/aa77a8c8-f41e-4658-bc1d-993d8b22972a
     * @return a URL path, if one is able to be generated, otherwise throws a UrlException
     */
    @Override
    public String generateUrlString() throws UrlException {
        try {
            DocumentMetadata metadata = documentVariant.released().isPresent()? documentVariant.released().get().metadata().get() : documentVariant.latestVersion().get().metadata().get();
            ProductVersion pv = metadata.productVersion().getReference();
            if (pv == null) {
                throw new UrlException("Document does not have associated product/version metadata.");
            }
            StringBuilder sb = new StringBuilder(getHost(documentVariant.getResourceResolver()));
            sb.append(URL_PREFIX)
                    .append(ServletUtils.toLanguageTag(getLocale(documentVariant))).append("/") // turns en_US into en-us which is likely a customer portal requirement (need to confirm)
                    .append(pv.getProduct().urlFragment().get()).append("/")
                    .append(pv.urlFragment().get()).append("/")
                    .append(getDocumentType(documentVariant)).append("/")
                    .append(documentVariant.uuid().get());
            return sb.toString();
        } catch (RepositoryException | NullPointerException e) {
            throw new UrlException(e);
        }
    }

    @Override
    public urlType getUrlType() {
        return documentVariant.released().isPresent() ? urlType.LIVE : urlType.PRELIVE;
    }
}
