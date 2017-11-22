package com.corefiling.zally.rule.collections

import io.swagger.models.ArrayModel
import io.swagger.models.Model
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.QueryParameter
import io.swagger.models.parameters.RefParameter
import io.swagger.models.properties.ArrayProperty
import io.swagger.models.properties.RefProperty
import io.swagger.parser.ResolverCache
import kotlin.collections.Map.Entry

fun Swagger.collections(): Map<String, Path> = collectionPaths(this)

fun collectionPaths(swagger: Swagger?): Map<String, Path> {
    return swagger?.paths.orEmpty().filter {
        entry: Entry<String, Path> -> detectCollection(swagger!!, entry.key, entry.value)
    }
}

fun detectCollection(swagger: Swagger, pattern: String, path: Path): Boolean {
    return detectCollectionByGetResponseReturningArray(swagger, path) ||
            detectCollectionByParameterizedSubresources(swagger, pattern) ||
            detectCollectionByPaginationQueryParameters(swagger, path)
}

fun detectCollectionByGetResponseReturningArray(swagger: Swagger, path: Path): Boolean {

    path.get?.responses?.values?.map(Response::getSchema)?.forEach { s ->
        if (s is ArrayProperty) return true

        if (s is RefProperty) {
            val resolver = ResolverCache(swagger, null, null)
            val model = resolver.loadRef(s.`$ref`, s.refFormat, Model::class.java)
            if (model is ArrayModel) return true
        }
    }

    return false
}

fun detectCollectionByParameterizedSubresources(swagger: Swagger, pattern: String): Boolean {

    val prefix = pattern + if (pattern.endsWith("/")) "{" else "/{"

    val result = swagger.paths.keys.filter { k ->
        k.startsWith(prefix)
    }

    return result.isNotEmpty()
}

fun detectCollectionByPaginationQueryParameters(swagger: Swagger, path: Path): Boolean {

    path.operations?.flatMap(Operation::getParameters)?.forEach { p ->
        var resolved = p

        if (p is RefParameter) {
            val resolver = ResolverCache(swagger, null, null)
            resolved = resolver.loadRef(p.`$ref`, p.refFormat, Parameter::class.java)
        }

        if (resolved is QueryParameter) {
            if (p.name == "pageSize" || p.name == "pageNumber") {
                return true
            }
        }
    }

    return false
}
