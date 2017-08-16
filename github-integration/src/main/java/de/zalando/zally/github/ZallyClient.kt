package de.zalando.zally.github

import de.zalando.zally.github.dto.ApiDefinitionResponse
import feign.Headers
import feign.RequestLine

interface ZallyClient {

    @RequestLine("POST /api-violations")
    @Headers("Content-Type: application/json")
    fun validate(content: String): ApiDefinitionResponse

}