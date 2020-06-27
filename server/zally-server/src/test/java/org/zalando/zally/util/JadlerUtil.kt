package org.zalando.zally.util

import net.jadler.Jadler.onRequest
import net.jadler.Jadler.port
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE

object JadlerUtil {

    @JvmOverloads
    fun stubResource(
        resourceName: String,
        responseBody: String = resourceToString(resourceName),
        contentType: String = APPLICATION_JSON_VALUE
    ): String {
        return stubResource(resourceName, responseBody, OK.value(), contentType)
    }

    fun stubResource(resourceName: String, responseBody: String, status: Int, contentType: String): String {
        val url = String.format("http://localhost:%d/%s", port(), resourceName)

        onRequest()
            .havingMethodEqualTo(GET.name)
            .havingPathEqualTo("/$resourceName")
            .respond()
            .withStatus(status)
            .withHeader(CONTENT_TYPE, contentType)
            .withBody(responseBody)

        return url
    }

    fun stubNotFound(): String {
        val remotePath = "/abcde.yaml"
        val url = "http://localhost:" + port() + remotePath

        onRequest()
            .havingMethodEqualTo(GET.name)
            .havingPathEqualTo(remotePath)
            .respond()
            .withStatus(NOT_FOUND.value())
            .withHeader(CONTENT_TYPE, TEXT_PLAIN_VALUE)
            .withBody("NotFound")

        return url
    }
}
