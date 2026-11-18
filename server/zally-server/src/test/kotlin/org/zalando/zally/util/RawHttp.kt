package org.zalando.zally.util

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Plain `java.net.http` client for tests that must see the response exactly as sent: no content negotiation, no
 * redirects, no retries. `TestRestTemplate` cannot retry streamed 401 responses and hides headers behind its converters.
 */
object RawHttp {
    class Response(
        val status: Int,
        val headers: Map<String, List<String>>,
        val body: String,
    ) {
        fun header(name: String): String? = headers[name.lowercase()]?.firstOrNull()

        val contentType: String? get() = header("content-type")
    }

    private val client: HttpClient = HttpClient.newHttpClient()

    fun send(
        port: Int,
        method: String,
        path: String,
        token: String? = null,
        body: String? = null,
        contentType: String? = "application/json",
        accept: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): Response {
        val builder = HttpRequest.newBuilder(URI("http://localhost:$port$path"))
        token?.let { builder.header("Authorization", "Bearer $it") }
        accept?.let { builder.header("Accept", it) }
        if (body != null && contentType != null) builder.header("Content-Type", contentType)
        headers.forEach { (name, value) -> builder.header(name, value) }
        builder.method(
            method,
            body?.let { HttpRequest.BodyPublishers.ofString(it) } ?: HttpRequest.BodyPublishers.noBody(),
        )
        val response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
        return Response(response.statusCode(), response.headers().map(), response.body())
    }
}
