package org.example.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Safelist;

public class StringUtil {
    public static String strip(String original, String charsToRemove) {
        if (original == null) {
            return null;
        }

        int end = original.length();
        int start = 0;
        char[] val = original.toCharArray();
        while (start < end && charsToRemove.indexOf(val[start]) >= 0) {
            start++;
        }
        while (start < end && charsToRemove.indexOf(val[end - 1]) >= 0) {
            end--;
        }
        return ((start > 0) || (end < original.length())) ? original.substring(start, end) : original;
    }

    public static String htmlSanitize(String original) {
        String text = Jsoup.clean(original, Safelist.simpleText());
        Document document = Jsoup.parseBodyFragment(text);
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        return document.body().text();
    }
}