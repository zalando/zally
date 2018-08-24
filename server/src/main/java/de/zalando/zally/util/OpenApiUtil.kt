package de.zalando.zally.util

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse

data class HeaderElement(
    val name: String,
    val element: Any
)

fun OpenAPI.getAllHeaders(): Set<HeaderElement> {

    fun Collection<Parameter>?.extractHeaders() = orEmpty()
        .filter { it.`in` == "header" }
        .map { HeaderElement(it.name, it) }
        .toSet()

    fun Collection<ApiResponse>?.extractHeaders() = orEmpty()
        .flatMap { it.headers.orEmpty().entries }
        .map { HeaderElement(it.key, it.value) }
        .toSet()

    val fromComponentsParams = components.parameters.orEmpty().values.extractHeaders()

    val fromPaths = paths.orEmpty().flatMap { (_, path) ->
        val fromPathParameters = path.parameters.extractHeaders()
        val fromOperations = path.readOperations().flatMap { operation ->
            val fromOpParams = operation.parameters.extractHeaders()
            val fromOpResponses = operation.responses.orEmpty().values.extractHeaders()
            fromOpParams + fromOpResponses
        }
        fromPathParameters + fromOperations
    }

    val fromComponentsHeaders = components.headers.orEmpty().map { HeaderElement(it.key, it.value) }

    return fromComponentsParams + fromPaths + fromComponentsHeaders
}

/**
 * Returns all defined schemas of an API specification
 * @return a collection of schemas
 */
fun OpenAPI.getAllSchemas(): Collection<Schema<Any>> = this.components.schemas.orEmpty().values +
    this.components.responses.values.flatMap { it.content.values.mapNotNull { it.schema } } +
    this.components.requestBodies.values.flatMap { it.content.values.mapNotNull { it.schema } } +
    this.paths.orEmpty().flatMap {
        it.value.readOperations().flatMap { it.parameters.orEmpty().mapNotNull { it.schema } }
    } +
    this.paths.orEmpty().flatMap {
        it.value.readOperations().flatMap {
            it.responses.orEmpty().flatMap { it.value.content.orEmpty().values.mapNotNull { it.schema } }
        }
    } +
    this.paths.orEmpty().flatMap {
        it.value.readOperations().flatMap { it.requestBody?.content.orEmpty().values.mapNotNull { it.schema } }
    }

/**
 * Traverses the schemas and returns all included schemas
 * @return a collection of all transitive schemas
 */
fun OpenAPI.getAllTransitiveSchemas(): Set<Schema<Any>> {
    fun isPrimitive(schema: Schema<Any>): Boolean = schema.properties.orEmpty().isEmpty()
    val collector = mutableSetOf<Schema<Any>>()

    tailrec fun traverseSchemas(schemasToScan: Collection<Schema<Any>>) {
        if (schemasToScan.isEmpty()) return

        val primitiveSchemas = schemasToScan.filter { isPrimitive(it) }
        val nonPrimitiveSchemas = schemasToScan.filterNot { isPrimitive(it) }

        collector.addAll(primitiveSchemas)
        traverseSchemas(nonPrimitiveSchemas.flatMap { it.properties.values })
    }

    traverseSchemas(this.getAllSchemas())

    return collector
}

/**
 * Traverses the schemas and returns all included properties and their names
 * @return a map (name -> schema) of all transitive properties
 */
fun OpenAPI.getAllProperties(): Map<String, Schema<Any>> {
    fun isPrimitive(schema: Schema<Any>): Boolean = schema.properties.orEmpty().isEmpty()
    val collector = mutableMapOf<String, Schema<Any>>()

    tailrec fun traverseSchemas(schemasToScan: Collection<Schema<Any>>) {
        if (schemasToScan.isEmpty()) return

        val properties = schemasToScan.flatMap { it.properties.orEmpty().entries }
        val primitiveSchemas = properties.filter { isPrimitive(it.value) }
        val nonPrimitiveSchemas = properties.filterNot { isPrimitive(it.value) }

        collector.putAll(primitiveSchemas.associateBy({ it.key }, { it.value }))
        traverseSchemas(nonPrimitiveSchemas.flatMap { it.value.properties.orEmpty().values })
    }

    traverseSchemas(this.getAllSchemas())

    return collector
}

/**
 * Returns all defined parameters of an API specification
 * @return a collection of parameters
 */
fun OpenAPI.getAllParameters(): Map<String, Parameter> = this.components.parameters.orEmpty() +
    this.paths.orEmpty().values.flatMap { it.parameters.orEmpty().mapNotNull { it.name to it } } +
    this.paths.orEmpty().values.flatMap {
        it.readOperations()
            .flatMap { it.parameters.orEmpty().mapNotNull { it.name to it } }
    }
