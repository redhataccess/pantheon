package com.redhat.pantheon.model;

import com.redhat.pantheon.model.document.Document;

import java.util.Optional;

public class Xref {

    private String anchor;
    private String label;
    private Document document;

    public Xref(Document document, String anchor, String label) {
        this.anchor = Optional.ofNullable(anchor).orElse("");
        this.label = Optional.ofNullable(label).orElse("");
        this.document = document;
    }

    public String getAnchor() {
        return anchor;
    }

    public String getLabel() {
        return label;
    }

    public Document getDocument() {
        return document;
    }
}
