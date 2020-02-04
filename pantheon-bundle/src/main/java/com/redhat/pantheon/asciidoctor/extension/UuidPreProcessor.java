package com.redhat.pantheon.asciidoctor.extension;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

import org.apache.sling.api.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UuidPreProcessor extends Preprocessor {   // (1)

    // private final String ascContent;

    // public UuidPreProcessor(String ascContent) {
    //     this.ascContent = ascContent;
    // }

    @Override
    public void process(Document document, PreprocessorReader reader) {
        
        List<String> lines = reader.readLines(); 
        List<String> newLines = new ArrayList<String>();
        String[] split;
        String uuid,newModulePath,addUUID;

        for (String line: lines) {
            if(line.startsWith("//pantheon-id:")){
                split = line.split(":");
                uuid = split[1];
                addUUID = ":"+uuid+",";
                newModulePath = split[2].replace(",",addUUID);
                newLines.add(newModulePath);
            }else {
                    newLines.add(line);
            }
        }
        reader.restoreLines(newLines);
    }
}