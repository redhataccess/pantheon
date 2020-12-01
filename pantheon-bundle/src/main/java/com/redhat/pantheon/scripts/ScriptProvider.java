package com.redhat.pantheon.scripts;

import java.util.stream.Stream;

public interface ScriptProvider {

    Stream<Script> getScripts();

}
