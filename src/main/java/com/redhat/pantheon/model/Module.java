package com.redhat.pantheon.model;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.via.ChildResource;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;

/**
 * Model class to represent modules
 */
@Model(
    adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class Module {

    @Self
    private Resource resource;

    @Inject @Named("sling:resourceType")
    @Default(values = "pantheon/modules")
    private String slingResourceType;

    @Inject @Named("jcr:created")
    private Calendar created;

    @Inject @Named("jcr:createdBy")
    private String createdBy;

    @Inject @Named("jcr:primaryType")
    private String primaryType;

    @Inject @Named("jcr:data")
    @Via(value = "asciidoc/jcr:content", type = ChildResource.class)
    private String asciidocContent;

    @Inject @Named("jcr:data")
    @Via(value = "cachedContent", type = ChildResource.class)
    private String cachedHtmlContent;

    @Inject
    private CachedContent cachedContent;

    public String getSlingResourceType() {
        return slingResourceType;
    }

    public Calendar getCreated() {
        return created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getPrimaryType() {
        return primaryType;
    }

    public String getAsciidocContent() {
        return asciidocContent;
    }

    public String getCachedHtmlContent() {
        return cachedHtmlContent;
    }

    public CachedContent getCachedContent() {
        return cachedContent;
    }

    /**
     * Model class to represent a Module's cached html content.
     * It is a nested class under Module as it only makes sense
     * for these nodes to be generated under a Module.
     */
    @Model(adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class CachedContent {

        @Self
        private Resource resource;

        @Inject @Named("pant:hash")
        private String hash;

        @Inject @Named("jcr:data")
        private String data;

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            resource.adaptTo(ModifiableValueMap.class).put("pant:hash", hash);
            this.hash = hash;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            resource.adaptTo(ModifiableValueMap.class).put("jcr:data", data);
            this.data = data;
        }
    }
}
