package com.redhat.pantheon.asciidoctor.extension;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import com.redhat.pantheon.jcr.JcrQueryHelper;
import org.apache.sling.api.resource.ResourceResolver;
import java.util.Optional;

import org.apache.sling.api.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UuidPreProcessor extends Preprocessor {   // (1)

    private static Resource module;
    private static final Logger log = LoggerFactory.getLogger(UuidPreProcessor.class);
    private static String newModulePath;

    public UuidPreProcessor(Resource module) {
        this.module = module;
    }

    @Override
    public void process(Document document, PreprocessorReader reader) {
        
        List<String> lines = reader.readLines(); 
        List<String> newLines = new ArrayList<String>();
        String[] split;
        String uuid, newLink;
        
        for (String line: lines) {
            if(line.startsWith("xref:")){
                split = line.split(",pantheon-id=");
                uuid = split[1].replace(split[1].substring(split[1].length()-1), "");                
                resolveActualPath(module.getResourceResolver(), uuid);
                newLink = split[0].replaceAll(":.*?\\[", ":"+newModulePath+"[");                
                newLink = newLink + "]";
                newLines.add(newLink);
            }else {
                    newLines.add(line);
            }
        }
        reader.restoreLines(newLines);
    }

    private static void resolveActualPath(ResourceResolver resolver, String uuid) {
        JcrQueryHelper qh = new JcrQueryHelper(resolver);        
        try {
                Optional<Resource> result = 
                    qh.query("select * from [nt:base] WHERE [jcr:uuid] = '" + uuid + "'")
                    .findFirst();                
                
                result.ifPresent(output -> {                
                    assignValue(output.getPath());
                    log.info("result:", output.getPath());                 
                });
            }catch (Exception e) {
                e.printStackTrace();
            }
        };

    private static void assignValue(String path){
            newModulePath = path + ".preview";
            log.info("newPath2:"+ newModulePath);
        }
    }