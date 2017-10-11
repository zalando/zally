package de.zalando.zally.rule

import com.typesafe.config.Config
import de.zalando.zally.rule.SpecPointerProvider.forOperation
import de.zalando.zally.rule.SpecPointerProvider.forPath
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter

abstract class HttpHeadersRule(rulesConfig: Config) : SwaggerRule() {

    private data class Header(
            val path: String,
            val name: String,
            val specPointer: String)

    private val headersWhitelist = rulesConfig.getStringList(HttpHeadersRule::class.simpleName + ".whitelist").toSet()

    protected abstract fun createViolation(paths: List<String>, specPointers: List<String>): Violation

    protected abstract fun isViolation(header: String): Boolean

    override fun validate(swagger: Swagger): Violation? {
        fun Map<String, Parameter>?.extractHeaders(path: String, specPointer: String): List<Header> =
                orEmpty()
                        .filter { (_, parameter) -> parameter.`in` == "header" }
                        .map { (paramName, parameter) ->
                            Header(path + "/$paramName", parameter.name, "$specPointer/$paramName/name")
                        }

        fun Collection<Parameter>?.extractHeaders(path: String, specPointer: String): List<Header> =
                orEmpty().withIndex()
                        .filter { (_, parameter) -> parameter.`in` == "header" }
                        .map { (paramIdx, parameter) ->
                            Header(path, parameter.name, "$specPointer/$paramIdx/name")
                        }

        fun Map<String, Response>?.extractHeaders(path: String, specPointer: String): List<Header> =
                orEmpty().flatMap { (responseCode, resp) ->
                    resp.headers?.keys.orEmpty().map { headerName ->
                        Header(path + "/$responseCode", headerName, "$specPointer/$responseCode/headers/$headerName")
                    }
                }

        val fromParams = swagger.parameters.extractHeaders("/parameters", "/parameters")

        val fromPaths = swagger.paths.orEmpty().entries.flatMap { (pathName, path) ->
            // Processing path.parameters actually has no sense since swagger parser spoils it in PathProcessor.processPaths()
            val pathGlobal = path.parameters.extractHeaders(pathName, "${forPath(pathName)}/parameters")
            val perOperation = path.operations.flatMap { operation ->
                val opPointer = forOperation(pathName, path, operation)
                operation.parameters.extractHeaders(pathName, "$opPointer/parameters") +
                        operation.responses.extractHeaders(pathName, "$opPointer/responses")
            }
            pathGlobal + perOperation
        }
        val allHeaders = fromParams + fromPaths
        val violatedHeaders = allHeaders
                .filter { header -> header.name !in headersWhitelist && isViolation(header.name) }
        val paths = violatedHeaders.map { (path, headerName) -> "$path $headerName" }
        val specPointers = violatedHeaders.map { it.specPointer }
        return if (paths.isNotEmpty()) createViolation(paths, specPointers) else null
    }
}
