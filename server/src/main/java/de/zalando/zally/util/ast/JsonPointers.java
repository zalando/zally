package de.zalando.zally.util.ast;

import com.fasterxml.jackson.core.JsonPointer;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
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
public final class JsonPointers {
    private JsonPointers() {
    }

    private static final List<Function<String, String>> POINTER_FUNCTIONS = Arrays.asList(
        createFn(compile("^/servers/.*$"), "/basePath"),
        createFn(compile("^/components/schemas/(.*)$"), "/definitions/%s"),
        createFn(compile("^/components/responses/(.*)$"), "/responses/%s"),
        createFn(compile("^/components/parameters/(.*)$"), "/parameters/%s"),
        createFn(compile("^/components/securitySchemes/(.*)$"), "/securityDefinitions/%s"),
        createFn(compile("^/paths/(.+/responses/.+)/content/.+/(schema.*)$"), "/paths/%s/%s"),

        // VERB/responses/STATUS_CODE/content/MEDIA_TYPE --> VERB/responses/STATUS_CODE
        // Could also be VERB/produces but this information is lost from Swagger 2 to OpenAPI 3 conversion.
        createFn(compile("^/paths/(.+/responses/.+)/content/[^/]*$"), "/paths/%s"),

        // VERB/requestBody/content/MEDIA_TYPE --> VERB/consumes
        createFn(compile("^/paths/(.*)/requestBody/content/[^/]*$"),"/paths/%s/consumes")
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
    public static JsonPointer convertPointer(JsonPointer pointer) {
        if (pointer != null) {
            for (Function<String, String> fn : POINTER_FUNCTIONS) {
                String convertedPointer = fn.apply(pointer.toString());
                if (convertedPointer != null) {
                    return JsonPointer.compile(convertedPointer);
                }
            }
        }
        return null;
    }

    protected static JsonPointer escape(final Method method, Object... arguments) {
        String name = method.getName();
        if (arguments.length > 0) {
            name = name.concat(Objects.toString(arguments[0]));
        }
        if (name.startsWith("get")) {
            name = name.substring(3);
            name = name.substring(0, 1).toLowerCase().concat(name.substring(1));
        }
        return escape(name);
    }

    protected static JsonPointer escape(final String unescaped) {
        // https://tools.ietf.org/html/rfc6901
        final String escaped = "/" + unescaped
            .replace("~", "~0")
            .replace("/", "~1");
        return JsonPointer.compile(escaped);
    }

    public static JsonPointer empty() {
        return JsonPointer.compile("");
    }
}
