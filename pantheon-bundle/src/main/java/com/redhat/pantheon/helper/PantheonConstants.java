package com.redhat.pantheon.helper;

import java.util.HashSet;
import java.util.Set;

public class PantheonConstants {
    public static final String ADOC_LEVELOFFSET = "leveloffset";
    public static final String MACRO_INCLUDE = "pantheon-include";
    public static final String PARAM_RERENDER = "rerender";
    public static final String PARAM_DRAFT = "draft";
    public static final String PARAM_LOCALE = "locale";
    public static final String PARAM_VARIANT = "variant";
    public static final String PANTHEON_PUBLISHERS = "pantheon-publishers";
    public static final String RESOURCE_TYPE_MODULE = "pantheon/module";
    public static final String RESOURCE_TYPE_ASSEMBLY = "pantheon/assembly";
    public static final String RESOURCE_TYPE_MODULEVARIANT = "pantheon/moduleVariant";
    public static final String RESOURCE_TYPE_MODULEVERSION = "pantheon/moduleVersion";
    public static final String RESOURCE_TYPE_ASSEMBLYVARIANT = "pantheon/assemblyVariant";
    public static final String RESOURCE_TYPE_ASSEMBLYVERSION = "pantheon/assemblyVersion";
    public static final String JCR_TYPE_MODULE = "pant:module";
    public static final String JCR_TYPE_ASSEMBLY = "pant:assembly";
    public static final String JCR_TYPE_MODULEVARIANT = "pant:moduleVariant";
    public static final String JCR_TYPE_ASSEMBLYVARIANT = "pant:assemblyVariant";
    public static final String JCR_TYPE_MODULEVERSION = "pant:moduleVersion";
    public static final String JCR_TYPE_ASSEMBLYVERSION = "pant:assemblyVersion";

    public static final String LATEST_SUFFIX = "/latest";
    public static final Set<String> RELEASED_SUFFIXES = new HashSet<>();

    public static final String XML_DOCUMENT_VERSION = "1.0";
    public static final String SITEMAP_NAMESPACE = "http://www.sitemaps.org/schemas/sitemap/0.9";
    public static final String SITE_MAP = "sitemap";
    public static final String SITEMAP_EXTENSION = "xml";
    public static final String URL_SET = "urlset";
    public static final String URL = "url";
    public static final String LOC = "loc";
    public static final String LAST_MOD = "lastmod";
    public static final String UTF_8 = "utf-8";
    public static final String XML_MIME_TYPE = "application/xml";

    public static final String SLING_SERVLET_DEFAULT = "sling/servlet/default";
    public static final String SLING_SERVLET_METHOD_GET = "GET";
    public static final String VIEW_URI = "view_uri";
    public static final String PORTAL_URL = "PORTAL_URL";
    public static final String VALID_XREF = "valid_Xref";
    public static final String TYPE_XREF = "xref";

    static {
        RELEASED_SUFFIXES.add("/released");
        RELEASED_SUFFIXES.add("/");
        RELEASED_SUFFIXES.add("");
        RELEASED_SUFFIXES.add(null);
    }
}
