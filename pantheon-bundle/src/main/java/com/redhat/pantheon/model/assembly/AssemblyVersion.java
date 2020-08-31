package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.Field;
import com.redhat.pantheon.model.api.FileResource;
import com.redhat.pantheon.model.api.SlingModel;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.document.AckStatus;
import com.redhat.pantheon.model.document.DocumentVariant;
import com.redhat.pantheon.model.document.DocumentVersion;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Named;

/**
 * A {@link SlingModel} which describes the structure for an assembly version.
 * Contains all the properties and content for the state of a given assembly at
 * a given time. AssemblyVersions should differ in content when part of the same
 * parent, but this is not validated.
 */
@JcrPrimaryType("pant:assemblyVersion")
public interface AssemblyVersion extends DocumentVersion {

    @Named("jcr:uuid")
    Field<String> uuid();

    @Named("pant:hash")
    Field<String> hash();

    @Named("cached_html")
    Child<FileResource> cachedHtml();

    Child<AssemblyMetadata> metadata();

    @Named("ack_status")
    Child<AckStatus> ackStatus();

    Child<AssemblyContent> content();

    @Override
    AssemblyVariant getParent();

    default void consumeTableOfContents(TableOfContents toc) {
        AssemblyContent asmContent = content().getOrCreate();
        int i = 0;
        for (TableOfContents.Entry entry : toc.getEntries()) {
            AssemblyPage p = asmContent.page(i++).getOrCreate();
            p.title().set(entry.getModuleVariant().getName());
            String title = entry.getModuleVariant()
                    .draft().getOrCreate()
                    .metadata().getOrCreate()
                    .title().get();
            if (title != null && !title.isEmpty()) {
                p.title().set(title);
            }
            p.moduleVariant().set(entry.getModuleVariant().uuid().get());
            p.leveloffset().set(entry.getLevelOffset());
        }
    }
}
