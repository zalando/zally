package de.zalando.zally.util

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import java.util.Objects

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

    val fromComponentsParams = components?.parameters?.values.extractHeaders()

    val fromPaths = paths.orEmpty().flatMap { (_, path) ->
        val fromPathParameters = path?.parameters.extractHeaders()
        val fromOperations = path?.readOperations().orEmpty().flatMap { operation ->
            val fromOpParams = operation?.parameters.extractHeaders()
            val fromOpResponses = operation?.responses?.values.extractHeaders()
            fromOpParams + fromOpResponses
        }
        fromPathParameters + fromOperations
    }

    val fromComponentsHeaders = components?.headers.orEmpty().map { HeaderElement(it.key, it.value) }

    return fromComponentsParams + fromPaths + fromComponentsHeaders
}

/**
 * Returns all defined schemas of an API specification
 * @return a collection of schemas
 */
fun OpenAPI.getAllSchemas(): Collection<Schema<Any>> = this.components?.schemas.orEmpty().values +
    this.components?.parameters.orEmpty().mapNotNull { it.value.schema } +
    this.components?.responses.orEmpty().values.flatMap { it.content.orEmpty().values.mapNotNull { it.schema } } +
    this.components?.requestBodies.orEmpty().values.flatMap { it.content.orEmpty().values.mapNotNull { it.schema } } +
    this.paths.orEmpty().flatMap {
        it.value?.readOperations().orEmpty().flatMap { it.parameters.orEmpty().mapNotNull { it.schema } }
    } +
    this.paths.orEmpty().flatMap {
        it.value?.readOperations().orEmpty().flatMap {
            it.responses.orEmpty().flatMap { it.value.content.orEmpty().values.mapNotNull { it.schema } }
        }
    } +
    this.paths.orEmpty().flatMap {
        it.value?.readOperations().orEmpty()
            .flatMap { it.requestBody?.content.orEmpty().values.mapNotNull { it.schema } }
    }

/**
 * Traverses the schemas and returns all included schemas
 * @return a collection of all transitive schemas
 */
fun OpenAPI.getAllTransitiveSchemas(): Collection<Schema<Any>> {
    fun isPrimitive(schema: Schema<Any>): Boolean = schema.properties.orEmpty().isEmpty()
    val collector = mutableMapOf<Int, Schema<Any>>()

    tailrec fun traverseSchemas(schemasToScan: Collection<Schema<Any>>) {
        if (schemasToScan.isEmpty()) return

        val primitiveSchemas = schemasToScan.filter { isPrimitive(it) }
        val nonPrimitiveSchemas = schemasToScan.filterNot { isPrimitive(it) }

        primitiveSchemas.forEach { schema -> collector[schema.customHash()] = schema }
        traverseSchemas(nonPrimitiveSchemas.flatMap { it.properties.values })
    }

    traverseSchemas(this.getAllSchemas())

    return collector.values
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
fun OpenAPI.getAllParameters(): Map<String, Parameter> = this.components?.parameters.orEmpty() +
    this.paths.orEmpty().values.flatMap { it?.parameters.orEmpty().mapNotNull { it.name to it } } +
    this.paths.orEmpty().values.flatMap {
        it?.readOperations().orEmpty().flatMap { it?.parameters.orEmpty().mapNotNull { it.name to it } }
    }

/**
 * Calculates custom hash to avoid calling the hash of the parent schema.
 * E.g. io.swagger.v3.oas.models.media.ArraySchema#hashCode() calls super#hashCode()
 * which fails for recursive data structures (endless loop -> StackOverflow)
 */
private fun Schema<Any>.customHash(): Int = Objects.hash(
    title, multipleOf, maximum, exclusiveMaximum, minimum,
    exclusiveMinimum, maxLength, minLength, pattern, maxItems, minItems, uniqueItems, maxProperties, minProperties,
    required, type, not, properties, additionalProperties, description, format, `$ref`, nullable, readOnly, writeOnly,
    example, externalDocs, deprecated, xml, extensions, discriminator
)
