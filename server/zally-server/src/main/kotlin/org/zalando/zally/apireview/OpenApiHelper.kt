package org.zalando.zally.apireview

import io.swagger.parser.SwaggerParser

object OpenApiHelper {

    private const val API_ID_EXTENSION = "x-api-id"

    fun extractApiName(apiDefinition: String): String? = try {
        val openApi = SwaggerParser().readWithInfo(apiDefinition, true).swagger
        if (openApi == null || openApi.info == null) {
            null
        } else {
            openApi.info.title.trim { it <= ' ' }
        }
    } catch (e: Exception) {
        null
    }

    fun extractApiId(apiDefinition: String): String? = try {
        val openApi = SwaggerParser().readWithInfo(apiDefinition, true).swagger
        if (openApi == null || openApi.info == null || !openApi.info.vendorExtensions.containsKey(API_ID_EXTENSION)) {
            null
        } else {
            openApi.info.vendorExtensions[API_ID_EXTENSION] as String
        }
    } catch (e: Exception) {
        null
    }
}
