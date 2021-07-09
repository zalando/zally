package org.zalando.zally.apireview

import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.exception.InaccessibleResourceUrlException
import org.zalando.zally.exception.MissingApiDefinitionException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE
import org.springframework.http.MediaType.parseMediaTypes
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate

@Component
class ApiDefinitionReader(private val client: RestTemplate) {

    fun read(request: ApiDefinitionRequest): String =
        request.apiDefinitionString?.let { it }
            ?: request.apiDefinition?.let { it }
            ?: request.apiDefinitionUrl?.let { readFromUrl(it) }
            ?: throw MissingApiDefinitionException()

    private fun readFromUrl(url: String): String? = try {
        val headers = HttpHeaders()
        headers.accept = MEDIA_TYPE_WHITELIST

        val entity = HttpEntity<String>(null, headers)

        val response = client.exchange(removeSpecialCharactersSuffix(url), HttpMethod.GET, entity, String::class.java)

        val contentType = response.headers.contentType
        if (contentType != null && MEDIA_TYPE_WHITELIST.none { contentType.isCompatibleWith(it) }) {
            throw InaccessibleResourceUrlException(
                "Unexpected content type while retrieving api definition url: $contentType",
                UNSUPPORTED_MEDIA_TYPE
            )
        }

        response.body
    } catch (exception: HttpClientErrorException) {
        throw InaccessibleResourceUrlException(
            "${exception.message} while retrieving api definition url",
            exception.statusCode
        )
    } catch (exception: ResourceAccessException) {
        throw InaccessibleResourceUrlException(
            "Unknown host while retrieving api definition url: ${exception.cause?.message}",
            HttpStatus.NOT_FOUND
        )
    }

    private fun removeSpecialCharactersSuffix(url: String): String = when {
        url.endsWith(SPECIAL_CHARACTERS_SUFFIX) -> url.substring(0, url.length - SPECIAL_CHARACTERS_SUFFIX.length)
        else -> url
    }

    companion object {

        // some internal systems add these characters at the end of some urls, don't know why
        private const val SPECIAL_CHARACTERS_SUFFIX = "%3D%3D"

        // a whitelist of mime-types to accept when expecting JSON or YAML
        private val MEDIA_TYPE_WHITELIST = parseMediaTypes(
            listOf(
                // standard YAML mime-type plus variants
                "application/yaml",
                "application/x-yaml",
                "application/vnd.yaml",
                "text/yaml",
                "text/x-yaml",
                "text/vnd.yaml",

                // standard JSON mime-type plus variants
                "application/json",
                "application/javascript",
                "text/javascript",
                "text/x-javascript",
                "text/x-json",

                // github.com raw content pages issue text/plain content type for YAML
                "text/plain"
            )
        )
    }
}
