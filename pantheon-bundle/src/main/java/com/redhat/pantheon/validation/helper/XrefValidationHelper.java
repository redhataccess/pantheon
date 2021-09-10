package com.redhat.pantheon.validation.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class XrefValidationHelper {

    private static HashMap<String, ArrayList<String>> xRefs;

    private static List<String> urlList;

    public XrefValidationHelper() {
        xRefs = new HashMap<String, ArrayList<String>>();
        urlList = new ArrayList<>();
    }

    public XrefValidationHelper(List<String> urlList) {
        this.urlList = urlList;
    }

    public static HashMap<String, ArrayList<String>> getObjectsToValidate() {
        return xRefs;
    }

    public static void initList() {
        XrefValidationHelper.xRefs = new HashMap<String, ArrayList<String>>();
        XrefValidationHelper.urlList = new ArrayList<>();
    }

    public static void setObjectsToValidate(HashMap<String, ArrayList<String>> objectsToValidate) {
        XrefValidationHelper.xRefs = objectsToValidate;
    }

    public static void setxRefs(HashMap<String, ArrayList<String>> xRefs) {
        XrefValidationHelper.xRefs = xRefs;
    }

    public static List<String> getUrlList() {
        return XrefValidationHelper.urlList;
    }

    public static void setUrlList(List<String> urlList) {
        XrefValidationHelper.urlList = urlList;
    }
}
