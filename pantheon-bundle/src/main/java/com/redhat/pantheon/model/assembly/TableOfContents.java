package com.redhat.pantheon.model.assembly;

import com.redhat.pantheon.model.module.ModuleVariant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableOfContents {

    private List<Entry> entryList = new ArrayList<>();

    public void addEntry(int levelOffset, ModuleVariant moduleVariant) {
        entryList.add(new Entry(levelOffset, moduleVariant, entryList.size()));
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entryList);
    }

    public class Entry {
        private int levelOffset;
        private ModuleVariant moduleVariant;
        private int index;

        public Entry (int levelOffset, ModuleVariant moduleVariant, int index) {
            this.levelOffset = levelOffset;
            this.moduleVariant = moduleVariant;
            this.index = index;
        }

        public int getLevelOffset() {
            return levelOffset;
        }

        public void setLevelOffset(int levelOffset) {
            this.levelOffset = levelOffset;
        }

        public ModuleVariant getModuleVariant() {
            return moduleVariant;
        }

        public int getIndex() {
            return index;
        }
    }
}
