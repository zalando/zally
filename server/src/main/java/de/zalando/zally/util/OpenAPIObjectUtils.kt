package de.zalando.zally.util

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema

/**
 * Returns all defined schemas of an API specification
 * @param api OpenAPI 3 specification object
 * @return a collection of schemas
 */
fun allSchemas(api: OpenAPI): Collection<Schema<Any>> =
    api.components.schemas.orEmpty().values +
    api.components.responses.values.flatMap { it.content.values.mapNotNull { it.schema } } +
    api.components.requestBodies.values.flatMap { it.content.values.mapNotNull { it.schema } } +
    api.paths.orEmpty().flatMap {
        it.value.readOperations().flatMap { it.parameters.orEmpty().mapNotNull { it.schema } }
    } +
    api.paths.orEmpty().flatMap {
        it.value.readOperations().flatMap {
            it.responses.orEmpty().flatMap { it.value.content.orEmpty().values.mapNotNull { it.schema } }
        }
    } +
    api.paths.orEmpty().flatMap {
        it.value.readOperations().flatMap { it.requestBody?.content.orEmpty().values.mapNotNull { it.schema } }
    }
