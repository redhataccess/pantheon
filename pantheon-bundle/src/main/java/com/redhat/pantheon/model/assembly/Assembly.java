package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

import javax.inject.Named;
import java.util.Locale;

@JcrPrimaryType("pant:assembly")
public interface Assembly extends WorkspaceChild {

    @Named("jcr:uuid")
    Field<String> uuid();

    default Child<AssemblyLocale> assemblyLocale(Locale locale) {
        return child(locale.toString(), AssemblyLocale.class);
    }
}
