package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.model.api.Child;
import com.redhat.pantheon.model.api.OrderedFolder;
import com.redhat.pantheon.model.api.annotation.JcrPrimaryType;

@JcrPrimaryType("sling:OrderedFolder")
public interface AssemblyContent extends OrderedFolder {

    default Child<AssemblyPage> page(int index) {
        return child(String.valueOf(index), AssemblyPage.class);
    }
}
