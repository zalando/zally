package de.zalando.zally.util.extensions

import de.zalando.zally.util.ComponentRef
import io.swagger.v3.oas.models.Components
import io.swagger.v3.parser.util.RefUtils

@Suppress("UNCHECKED_CAST")
fun <T> Components.loadByRef(ref: String, expectedType: Class<T>): T? {
    val format = RefUtils.computeRefFormat(ref)
    if (RefUtils.isAnExternalRefFormat(format)) {
        throw IllegalArgumentException("External refs are not supported")
    }
    val componentRef = ComponentRef.parse(ref)
    return when (componentRef?.type) {
        "schemas" -> schemas.get(componentRef.name)
        "responses" -> responses.get(componentRef.name)
        "parameters" -> parameters.get(componentRef.name)
        "examples" -> examples.get(componentRef.name)
        "requestBodies" -> requestBodies.get(componentRef.name)
        "headers" -> headers.get(componentRef.name)
        "securitySchemes" -> securitySchemes.get(componentRef.name)
        "links" -> links.get(componentRef.name)
        "callbacks" -> callbacks.get(componentRef.name)
        "extensions" -> extensions.get(componentRef.name)
        else -> null
    } as T
}

fun Components.items(): List<Pair<String, Map<String, Any>>> =
        listOf(
                "schemas" to schemas,
                "responses" to responses,
                "parameters" to parameters,
                "examples" to examples,
                "requestBodies" to requestBodies,
                "headers" to headers,
                "securitySchemes" to securitySchemes,
                "links" to links,
                "callbacks" to callbacks,
                "extensions" to extensions
        )

