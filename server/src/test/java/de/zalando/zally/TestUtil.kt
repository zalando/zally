package de.zalando.zally

import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import de.zalando.zally.rule.ObjectTreeReader
import io.swagger.parser.OpenAPIParser
import io.swagger.parser.util.ClasspathHelper
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.parser.core.models.ParseOptions

val testConfig: Config by lazy {
    ConfigFactory.load("rules-config.conf")
}

fun getFixture(fileName: String): OpenAPI = OpenAPIParser().readLocation("fixtures/$fileName", null, ParseOptions()).openAPI

fun getResourceContent(fileName: String): String = ClasspathHelper.loadFileFromClasspath("fixtures/$fileName")

fun getResourceJson(fileName: String): JsonNode = ObjectTreeReader().read(getResourceContent(fileName))

fun swaggerWithPaths(vararg specificPaths: String): OpenAPI {
    val result = Paths()
    specificPaths.map { it to PathItem() }.toMap().forEach{(k, v) -> result.addPathItem(k, v)}
    return OpenAPI().apply {
        paths = result
    }
}

fun swaggerWithHeaderParams(vararg names: String): OpenAPI {
/*    OpenAPI().apply {
        parameters = names.map { header ->
            header to HeaderParameter().apply { name = header }
        }.toMap()
    }*/
    //TODO implement it
    return OpenAPI()
}

fun swaggerWithDefinitions(vararg defs: Pair<String, List<String>>): OpenAPI {
/*    Swagger().apply {
        definitions = defs.map { def ->
            def.first to ModelImpl().apply {
                properties = def.second.map { prop -> prop to StringProperty() }.toMap()
            }
        }.toMap()
    }*/
    //TODO implement it
    return OpenAPI()
}

fun swaggerWithOperations(operations: Map<String, Iterable<String>>): OpenAPI {
/*    Swagger().apply {
        val path = Path()
        operations.forEach { method, statuses ->
            val operation = Operation().apply {
                statuses.forEach { addResponse(it, Response()) }
            }
            path.set(method, operation)
        }
        paths = mapOf("/test" to path)
    }*/
    //TODO implement it
    return OpenAPI()
}
