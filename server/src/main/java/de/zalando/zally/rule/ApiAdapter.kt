package de.zalando.zally.rule

import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI

data class ApiAdapter(val swagger: Swagger?, val openAPI: OpenAPI) {
    fun isV2(): Boolean {
        return swagger != null
    }

    val vendorExtensions: MutableMap<String, Any>? = swagger?.vendorExtensions ?: openAPI.extensions
}
