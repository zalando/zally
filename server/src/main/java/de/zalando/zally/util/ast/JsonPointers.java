package de.zalando.zally.util.ast;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Utility to convert OpenAPI 3 JSON pointers to Swagger 2 pointers.
 */
public final class JsonPointers {
    private JsonPointers() {
    }

    private static final List<Function<String, String>> POINTER_FUNCTIONS = Arrays.asList(
        createFn(compile("^#/servers/.*$"), "#/basePath"),
        createFn(compile("^#/components/schemas/(.*)$"), "#/definitions/%s"),
        createFn(compile("^#/components/responses/(.*)$"), "#/responses/%s"),
        createFn(compile("^#/components/parameters/(.*)$"), "#/parameters/%s"),
        createFn(compile("^#/components/securitySchemes/(.*)$"), "#/securityDefinitions/%s"),
        createFn(compile("^#/paths/(.+/responses/.+)/content/.+/(schema.*)$"), "#/paths/%s/%s")
    );

    private static Function<String, String> createFn(Pattern pattern, String pointerOut) {
        return (pointerIn) -> {
            Matcher matcher = pattern.matcher(pointerIn);
            if (matcher.find()) {
                String[] matches = new String[matcher.groupCount()];
                for (int i = 0; i < matches.length; i++) {
                    matches[i] = matcher.group(1 + i);
                }
                return String.format(pointerOut, (Object[]) matches);
            }
            return null;
        };
    }

    /**
     * Convert an OpenAPI 3 JSON pointer to a Swagger 2 pointer.
     *
     * @param pointer OpenAPI 3 JSON pointer.
     * @return Equivalent Swagger 2 JSON pointer or null.
     */
    @Nullable
    public static String convertPointer(String pointer) {
        for (Function<String, String> fn : POINTER_FUNCTIONS) {
            String convertedPointer = fn.apply(pointer);
            if (convertedPointer != null) {
                return convertedPointer;
            }
        }
        return null;
    }
}
