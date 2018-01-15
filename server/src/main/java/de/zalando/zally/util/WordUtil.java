package de.zalando.zally.util;

import de.zalando.zally.external.jbossdna.Inflector;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class WordUtil {
    private static final Inflector INFLECTOR = new Inflector();
    private static final Set<String> PLURAL_WHITELIST = new HashSet<>(asList("vat", "content"));

    public static boolean isPlural(String word) {
        if (PLURAL_WHITELIST.contains(word)) {
            return true;
        }
        String singular = INFLECTOR.singularize(word);
        String plural = INFLECTOR.pluralize(word);
        return plural.equals(word) && !singular.equals(word);
    }
}
