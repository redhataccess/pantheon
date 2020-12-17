package com.redhat.pantheon.auth.keycloak.utils;

import org.apache.http.Header;

public class HttpResponse {

    private String body;
    private Header[] headers;
    private String reasonPhrase;
    private int statusCode;


    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public Header[] getHeaders() {
        return headers;
    }
    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }
    public String getReasonPhrase() {
        return reasonPhrase;
    }
    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }
    public int getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }


}
