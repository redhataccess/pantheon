package com.redhat.pantheon.model.module;

import com.redhat.pantheon.model.api.WorkspaceChild;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;
import com.redhat.pantheon.model.api.Reference;
import com.redhat.pantheon.model.api.SlingModel;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.counting;

/**
 * A specific module locale node which houses asciidoc source and variants
 */
@JcrPrimaryType("pant:moduleLocale")
public interface ModuleLocale extends WorkspaceChild {

}
