package com.redhat.pantheon.extension.url;

import com.redhat.pantheon.model.document.DocumentVariant;

public abstract class UrlProvider {

    public enum urlType { LIVE, PRELIVE }

    protected final DocumentVariant documentVariant;

    public UrlProvider(DocumentVariant documentVariant) {
        this.documentVariant = documentVariant;
    }

    public abstract String generateUrlString() throws UrlException;
    public abstract urlType getUrlType();
}
