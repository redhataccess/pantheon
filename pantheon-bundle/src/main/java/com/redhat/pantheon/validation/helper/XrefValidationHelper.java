package com.redhat.pantheon.validation.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class XrefValidationHelper {

    private static Map<String, List<String>> xRefs = new HashMap<>();

    public static List<String> getObjectsToValidate(String uuid) {
        return xRefs.get(uuid);
    }

    public static void setObjectsToValidate(String uuid, List<String> objectsToValidate) {
        if(objectsToValidate.size()>0)
            xRefs.put(uuid, objectsToValidate);
    }
}
