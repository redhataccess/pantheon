package com.redhat.pantheon.extension.url;

import com.redhat.pantheon.model.document.DocumentVariant;

public class DrupalXrefProvider implements UrlProvider {

    @Override
    public String generateUrlString(DocumentVariant variant) {
        return "xref:" + new CustomerPortalUrlUuidProvider().generateUrlString(variant);
    }
}
