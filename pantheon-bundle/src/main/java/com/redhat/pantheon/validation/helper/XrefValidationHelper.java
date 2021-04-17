package com.redhat.pantheon.validation.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class XrefValidationHelper {

    private static List<String> xRefs;

    public static List<String> getObjectsToValidate() {
        return xRefs;
    }

    public static void initMap() {
        xRefs = new ArrayList<>();
    }

    public static void setObjectsToValidate(List<String> objectsToValidate) {
        if(objectsToValidate.size()>0){
                xRefs.addAll(objectsToValidate);
        }
    }
}
