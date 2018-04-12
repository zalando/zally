package de.zalando.zally.util.ast;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Utility to convert OpenAPI 3 JSON pointers to Swagger 2 pointers.
 */
final class JsonPointers {
    private JsonPointers() {
    }

    private static final List<Function<String, String>> POINTER_FUNCTIONS = Arrays.asList(
            createFn(compile("^#/servers/.*$"), "#/basePath"),
            createFn(compile("^#/components/schemas(?<path>/.*)$"), "#/definitions"),
            createFn(compile("^#/components/responses(?<path>/.*)$"), "#/responses"),
            createFn(compile("^#/components/parameters(?<path>/.*)$"), "#/parameters"),
            createFn(compile("^#/components/securitySchemes(?<path>/.*)$"), "#/securityDefinitions")
    );

    private static Function<String, String> createFn(Pattern pattern, String pointerOut) {
        return (pointerIn) -> {
            Matcher matcher = pattern.matcher(pointerIn);
            if (matcher.matches()) {
                String path = matcher.groupCount() > 0 ? Objects.toString(matcher.group("path"), "") : "";
                return pointerOut.concat(path);
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
    static String convertPointer(String pointer) {
        for (Function<String, String> fn : POINTER_FUNCTIONS) {
            String convertedPointer = fn.apply(pointer);
            if (convertedPointer != null) {
                return convertedPointer;
            }
        }
        return null;
    }
}
