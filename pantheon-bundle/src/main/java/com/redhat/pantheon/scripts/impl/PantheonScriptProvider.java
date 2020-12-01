package com.redhat.pantheon.scripts.impl;

import com.redhat.pantheon.scripts.Script;
import com.redhat.pantheon.scripts.ScriptProvider;
import jdk.nashorn.internal.ir.annotations.Reference;

import java.util.stream.Stream;

public class PantheonScriptProvider implements ScriptProvider {

    private SampleScript sampleScript;

    public PantheonScriptProvider(@Reference SampleScript sampleScript) {
        this.sampleScript = sampleScript;
    }

    @Override
    public Stream<Script> getScripts() {
        return Stream.of(
                sampleScript
        );
    }
}
