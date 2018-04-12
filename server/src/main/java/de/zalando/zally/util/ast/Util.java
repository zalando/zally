package de.zalando.zally.util.ast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class Util {
    static final Set<Class<?>> PRIMITIVES = new HashSet<>(Arrays.asList(
        String.class,
        Integer.class,
        Float.class,
        Double.class,
        Boolean.class,
        Enum.class
    ));

    static String rfc6901Encode(String s) {
        // https://tools.ietf.org/html/rfc6901
        return s.replace("~", "~0").replace("/", "~1");
    }

    static String getterNameToPointer(String name) {
        if (name.startsWith("get")) {
            String s = name.substring(3);
            return s.substring(0, 1).toLowerCase().concat(s.substring(1));
        }
        return name;
    }

    private Util() {
    }
}
