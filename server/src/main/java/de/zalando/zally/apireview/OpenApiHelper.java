package de.zalando.zally.apireview;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

public class OpenApiHelper {

    private static String API_ID_EXTENSION = "x-api-id";

    private OpenApiHelper() {}

    public static String extractApiName(String apiDefinition) {
        try {
            Swagger openApi = new SwaggerParser().readWithInfo(apiDefinition, true).getSwagger();
            if (openApi == null || openApi.getInfo() == null) {
                return null;
            }
            return openApi.getInfo().getTitle().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public static String extractApiId(String apiDefinition) {
        try {
            Swagger openApi = new SwaggerParser().readWithInfo(apiDefinition, true).getSwagger();
            if (openApi == null || openApi.getInfo() == null || !openApi.getInfo().getVendorExtensions().containsKey(API_ID_EXTENSION)) {
                return null;
            }
            return (String) openApi.getInfo().getVendorExtensions().get(API_ID_EXTENSION);
        } catch (Exception e) {
            return null;
        }
    }
}
