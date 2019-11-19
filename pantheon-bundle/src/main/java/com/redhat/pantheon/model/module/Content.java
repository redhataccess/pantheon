package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.v2.FileResource;
import com.redhat.pantheon.model.api.v2.Child;
import com.redhat.pantheon.model.api.v2.Field;
import com.redhat.pantheon.model.api.v2.SlingModel;

import javax.inject.Named;

/**
 * Models a single instance of a module's content. Multiple content instances may be found on a
 * given module representing several content versions.
 */
public interface Content extends SlingModel {

    @Named("asciidoc/jcr:content/jcr:data")
    Field<String> asciidocContent();

    Child<CachedContent> cachedHtml();

    Child<FileResource> asciidoc();

    /**
     * A child resource for a {@link ModuleVersion} which contains cached data
     * when a resource is generated.
     */
    interface CachedContent extends SlingModel {

        @Named("pant:hash")
        Field<String> hash();

        @Named("jcr:data")
        Field<String> data();
    }
}
