package com.redhat.pantheon.model.Document;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.WorkspaceChild;

import javax.inject.Named;
import java.util.Locale;

public interface Document extends WorkspaceChild {
    @Named("jcr:uuid")
    Field<String> uuid();

    default Child<DocumentLocale> documentLocale(Locale locale) {
        return child(locale.toString(), DocumentLocale.class);
    }
}
