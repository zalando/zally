package org.zalando.zally.apireview

import io.swagger.parser.SwaggerParser

object EndpointCounter {

    fun count(apiDefinition: String): Int = try {
        val swagger = SwaggerParser().parse(apiDefinition)
        swagger?.paths?.size ?: 0
    } catch (e: Exception) {
        0
    }
}
