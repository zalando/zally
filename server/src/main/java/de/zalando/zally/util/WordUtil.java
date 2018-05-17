package de.zalando.zally.util;

import javatools.parsers.PlingStemmer;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class WordUtil {
    private static final Set<String> PLURAL_WHITELIST = new HashSet<>(asList("vat", "apis"));

    public static boolean isPlural(String word) {
        if (PLURAL_WHITELIST.contains(word)) {
            return true;
        }
        return PlingStemmer.isPlural(word);
    }
}
