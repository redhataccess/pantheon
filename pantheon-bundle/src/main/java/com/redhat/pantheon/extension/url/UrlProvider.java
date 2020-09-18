package com.redhat.pantheon.extension.url;

import com.redhat.pantheon.model.document.DocumentVariant;

public interface UrlProvider {
    String generateUrlString(DocumentVariant variant);
}
