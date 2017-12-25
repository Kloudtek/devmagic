package com.kloudtek.devmagic.util;

import java.util.ArrayList;
import java.util.List;

public class FilterUtils {
    public static List<String> filter(List<String> list, List<String> includes, List<String> excludes) {
        ArrayList<String> filtered = new ArrayList<>();
        for (String val : list) {
            if ((includes == null || includes.isEmpty() || matches(val, includes)) && !matches(val, excludes)) {
                filtered.add(val);
            }
        }
        return filtered;
    }

    private static boolean matches(String val, List<String> regexList) {
        if (regexList != null) {
            for (String include : regexList) {
                if (val.matches(include)) {
                    return true;
                }
            }
        }
        return false;
    }
}
