package de.zalando.zally.util.extensions

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.MediaType

fun Operation.filterByContent(handler: (String, MediaType) -> Boolean): List<Pair<String, MediaType>> {
    return responses
            .flatMap { (_, response) ->
                response.content.entries.filter { (contentType, mediaType) ->
                    handler(contentType, mediaType)
                }
            }
            .map { (contentType, mediaType) -> contentType to mediaType }
}

fun Operation.allContentTypes(): List<String> = filterByContent { _, _ -> true }.map { (name, _) -> name }

val Operation.producesJson: Boolean
    get() = allContentTypes().contains("json")
