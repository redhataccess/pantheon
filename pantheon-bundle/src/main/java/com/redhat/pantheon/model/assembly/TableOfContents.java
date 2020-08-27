package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.model.module.ModuleVariant;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TableOfContents {

    private List<Entry> entryList = new LinkedList<>();

    public void addEntry(String levelOffset, ModuleVariant moduleVariant) {
        entryList.add(new Entry(levelOffset, moduleVariant, entryList.size()));
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entryList);
    }

    public class Entry {
        private String levelOffset;
        private ModuleVariant moduleVariant;
        private int index;

        public Entry (String levelOffset, ModuleVariant moduleVariant, int index) {
            this.levelOffset = levelOffset;
            this.moduleVariant = moduleVariant;
            this.index = index;
        }

        public String getLevelOffset() {
            return levelOffset;
        }

        public ModuleVariant getModuleVariant() {
            return moduleVariant;
        }

        public int getIndex() {
            return index;
        }
    }
}
