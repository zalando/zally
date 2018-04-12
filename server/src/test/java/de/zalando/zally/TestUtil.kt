package de.zalando.zally

import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import de.zalando.zally.rule.ObjectTreeReader
import io.swagger.models.ModelImpl
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.HeaderParameter
import io.swagger.models.properties.StringProperty
import io.swagger.parser.SwaggerParser
import io.swagger.parser.util.ClasspathHelper
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses

val testConfig: Config by lazy {
    ConfigFactory.load("rules-config.conf")
}

fun getFixture(fileName: String): Swagger = SwaggerParser().read("fixtures/$fileName")

fun getResourceContent(fileName: String): String = ClasspathHelper.loadFileFromClasspath("fixtures/$fileName")

fun getResourceJson(fileName: String): JsonNode = ObjectTreeReader().read(getResourceContent(fileName))

fun swaggerWithPaths(vararg specificPaths: String): Swagger =
    Swagger().apply {
        paths = specificPaths.map { it to Path() }.toMap()
    }

fun swaggerWithHeaderParams(vararg names: String) =
    Swagger().apply {
        parameters = names.map { header ->
            header to HeaderParameter().apply { name = header }
        }.toMap()
    }

fun swaggerWithDefinitions(vararg defs: Pair<String, List<String>>): Swagger =
    Swagger().apply {
        definitions = defs.map { def ->
            def.first to ModelImpl().apply {
                properties = def.second.map { prop -> prop to StringProperty() }.toMap()
            }
        }.toMap()
    }

fun swaggerWithOperations(operations: Map<String, Iterable<String>>): Swagger =
    Swagger().apply {
        val path = Path()
        operations.forEach { method, statuses ->
            val operation = Operation().apply {
                statuses.forEach { addResponse(it, Response()) }
            }
            path.set(method, operation)
        }
        paths = mapOf("/test" to path)
    }

fun openApiWithOperations(operations: Map<String, Iterable<String>>): OpenAPI =
    OpenAPI().apply {
        val pathItem = PathItem()
        operations.forEach { method, statuses ->
            val operation = io.swagger.v3.oas.models.Operation().apply {
                responses = ApiResponses()
                statuses.forEach {
                    responses.addApiResponse(it, ApiResponse())
                }
            }
            pathItem.operation(io.swagger.v3.oas.models.PathItem.HttpMethod.valueOf(method.toUpperCase()), operation)
        }
        paths = Paths()
        paths.addPathItem("/test", pathItem)
    }
