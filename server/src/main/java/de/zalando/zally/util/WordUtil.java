package de.zalando.zally.util;

import javatools.parsers.PlingStemmer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WordUtil {
    private static final Set<String> PLURAL_WHITELIST = new HashSet<>(Collections.singletonList("vat"));

    public static boolean isPlural(String word) {
        if (PLURAL_WHITELIST.contains(word)) {
            return true;
        }
        return PlingStemmer.isPlural(word);
    }
}
