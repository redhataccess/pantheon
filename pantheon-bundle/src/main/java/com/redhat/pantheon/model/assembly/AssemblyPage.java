package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.Reference;
import com.redhat.pantheon.model.workspace.WorkspaceChild;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.module.ModuleVariant;

import javax.inject.Named;

@JcrPrimaryType("pant:assemblyPage")
public interface AssemblyPage extends WorkspaceChild {

    @Named("pant:title")
    Field<String> title();

    @Named("pant:moduleVariantUuid")
    Reference<ModuleVariant> moduleVariant();

    @Named("pant:leveloffset")
    Field<Integer> leveloffset();

    @Named("cached_html")
    Child<FileResource> cachedHtml();
}
