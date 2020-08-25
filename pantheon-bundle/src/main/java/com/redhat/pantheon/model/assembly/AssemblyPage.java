package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.Reference;
import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.module.Module;

import javax.inject.Named;

@JcrPrimaryType("pant:assemblyPage")
public interface AssemblyPage extends WorkspaceChild {

    @Named("pant:title")
    Field<String> title();

    @Named("pant:moduleUuid")
    Reference<Module> module();

    @Named("pant:leveloffset")
    Field<Integer> leveloffset();

    @Named("cached_html")
    Child<FileResource> cachedHtml();
}
