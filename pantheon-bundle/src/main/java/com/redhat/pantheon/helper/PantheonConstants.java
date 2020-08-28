package com.redhat.pantheon.helper;

import java.util.HashSet;
import java.util.Set;

public class PantheonConstants {
    public static final String ADOC_LEVELOFFSET = "leveloffset";
    public static final String PARAM_RERENDER = "rerender";
    public static final String PARAM_DRAFT = "draft";
    public static final String PARAM_LOCALE = "locale";
    public static final String PARAM_VARIANT = "variant";
    public static final String PANTHEON_PUBLISHERS = "pantheon-publishers";
    public static final String RESOURCE_TYPE_MODULE = "pantheon/module";
    public static final String RESOURCE_TYPE_ASSEMBLY = "pantheon/assembly";
    public static final String RESOURCE_TYPE_MODULEVARIANT = "pantheon/moduleVariant";
    public static final String RESOURCE_TYPE_ASSEMBLYVARIANT = "pantheon/assemblyVariant";
    public static final String JCR_TYPE_MODULE = "pant:module";
    public static final String JCR_TYPE_ASSEMBLY = "pant:assembly";
    public static final String JCR_TYPE_MODULEVARIANT = "pant:moduleVariant";
    public static final String JCR_TYPE_ASSEMBLYVARIANT = "pant:assemblyVariant";

    public static final String LATEST_SUFFIX = "/latest";
    public static final Set<String> RELEASED_SUFFIXES = new HashSet<>();
    static {
        RELEASED_SUFFIXES.add("/released");
        RELEASED_SUFFIXES.add("/");
        RELEASED_SUFFIXES.add("");
        RELEASED_SUFFIXES.add(null);
    }
}
