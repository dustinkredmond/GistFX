package com.dustinredmond.utils;

public class StringUtils {

    public static String truncate(String string, int numChars) {
        return string.substring(0, Math.min(numChars, string.length()));
    }
}
