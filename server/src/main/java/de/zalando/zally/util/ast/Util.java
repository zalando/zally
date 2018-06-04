package de.zalando.zally.util.ast;

import java.util.Arrays;
import java.util.HashSet;
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

    private Util() {
    }
}
