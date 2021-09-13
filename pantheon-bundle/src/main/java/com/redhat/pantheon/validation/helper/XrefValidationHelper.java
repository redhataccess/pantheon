package com.redhat.pantheon.validation.helper;

import java.util.ArrayList;
import java.util.HashMap;


public class XrefValidationHelper {

    private static HashMap<String, ArrayList<String>> xRefs;

    public XrefValidationHelper() {
        xRefs = new HashMap<String, ArrayList<String>>();
    }

    public static HashMap<String, ArrayList<String>> getObjectsToValidate() {
        return xRefs;
    }

    public static void initList() {
        XrefValidationHelper.xRefs = new HashMap<String, ArrayList<String>>();
    }

    public static void setObjectsToValidate(HashMap<String, ArrayList<String>> objectsToValidate) {
        XrefValidationHelper.xRefs = objectsToValidate;
    }
}
