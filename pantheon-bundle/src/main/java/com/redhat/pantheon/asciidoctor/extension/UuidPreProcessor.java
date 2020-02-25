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
            //regex
            if(line.startsWith("xref:")){
                split = line.split(",pantheon-id=");
                uuid = split[1].replace(split[1].substring(split[1].length()-1), "");
                addUUID = ":" + uuid + "[";
                split[0] = split[0].replace("[",addUUID);                
                newModulePath = split[0] + "]";
                newLines.add(newModulePath);
            }else {
                    newLines.add(line);
            }
        }
        reader.restoreLines(newLines);
    }
}