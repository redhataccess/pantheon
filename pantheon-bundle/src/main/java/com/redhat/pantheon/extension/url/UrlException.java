package com.redhat.pantheon.extension.url;

public class UrlException extends Exception {

    public UrlException() {
        super();
    }

    public UrlException(String message) {
        super(message);
    }

    public UrlException(String message, Throwable cause) {
        super(message, cause);
    }

    public UrlException(Throwable cause) {
        super(cause);
    }

    protected UrlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
