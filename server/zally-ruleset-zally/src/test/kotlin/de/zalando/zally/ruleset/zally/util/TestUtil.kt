package de.zalando.zally.ruleset.zally.util

import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import de.zalando.zally.core.ObjectTreeReader
import io.swagger.parser.util.ClasspathHelper
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import java.io.StringReader

val testConfig: Config by lazy {
    ConfigFactory.load("rules-config.conf")
}

fun getConfigFromContent(content: String): Config = ConfigFactory.parseReader(StringReader(content))

fun getResourceContent(fileName: String): String = ClasspathHelper.loadFileFromClasspath("fixtures/$fileName")

fun getResourceJson(fileName: String): JsonNode = ObjectTreeReader().read(getResourceContent(fileName))

fun openApiWithOperations(operations: Map<String, Iterable<String>>): OpenAPI =
    OpenAPI().apply {
        val pathItem = PathItem()
        operations.forEach { (method, statuses) ->
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
