package de.zalando.zally.integration.zally

import feign.Headers
import feign.RequestLine

interface ZallyClient {

    @RequestLine("POST /api-violations")
    @Headers("Content-Type: application/json")
    fun validate(content: String): ApiDefinitionResponse
}