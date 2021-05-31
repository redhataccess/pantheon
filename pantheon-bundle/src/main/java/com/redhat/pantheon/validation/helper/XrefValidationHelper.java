package com.redhat.pantheon.validation.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class XrefValidationHelper {

    private static List<String> xRefs;

    private XrefValidationHelper() {
    }

    private static class SingletonHelper{
        private static final XrefValidationHelper INSTANCE = new XrefValidationHelper();
    }

    public static XrefValidationHelper getInstance(){
        return SingletonHelper.INSTANCE;
    }
    public List<String> getObjectsToValidate() {
        return xRefs;
    }

    public void initList() {
        xRefs = new ArrayList<>();
    }

    public void setObjectsToValidate(List<String> objectsToValidate) {
        if(objectsToValidate.size()>0){
            xRefs.addAll(objectsToValidate);
        }
    }
}
