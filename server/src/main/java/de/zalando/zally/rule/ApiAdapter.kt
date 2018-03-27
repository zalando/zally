package de.zalando.zally.rule

import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI

data class ApiAdapter(val swagger: Swagger?, val openAPI: OpenAPI) {

    fun isV2(): Boolean {
        return swagger != null
    }

    val vendorExtensions: MutableMap<String, Any>? = swagger?.vendorExtensions ?: openAPI.extensions


    /**
     * Executes validation with [Swagger] object.
     * If there is no Swagger object then return [Violation.UNSUPPORTED_API_VERSION]
     */
    fun withVersion2(handler: (Swagger) -> Violation?): Violation? =
            if (isV2()) {
                handler(swagger!!)
            } else Violation.UNSUPPORTED_API_VERSION

}
