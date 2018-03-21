package de.zalando.zally.rule

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse

abstract class HttpHeadersRule(rulesConfig: Config) {

    private val headersWhitelist = rulesConfig.getStringList(HttpHeadersRule::class.simpleName + ".whitelist").toSet()

    abstract fun createViolation(paths: List<String>): Violation

    abstract fun isViolation(header: String): Boolean

    open fun validate(adapter: ApiAdapter): Violation? {
        val swagger = adapter.openAPI

        fun Collection<Parameter>?.extractHeaders(path: String) =
                orEmpty().filter { it.`in` == "header" }.map { path to it.name }

        fun Collection<ApiResponse>?.extractHeaders(path: String) =
                orEmpty().flatMap { it.headers?.keys.orEmpty() }.map { path to it }

        val fromParams = swagger.components.parameters.orEmpty().values.extractHeaders("parameters")

        val fromPaths = swagger
                .paths
                .orEmpty()
                .entries
                .flatMap { (name, path) ->
                    path.parameters.extractHeaders(name) + path.readOperations().flatMap { operation ->
                        operation.parameters.extractHeaders(name) + operation.responses.values.extractHeaders(name)
                    }
                }
        val allHeaders = fromParams + fromPaths
        val paths = allHeaders
                .filter { it.second !in headersWhitelist && isViolation(it.second) }
                .map { "${it.first} ${it.second}" }
                .toSet()
                .toList()
        return if (paths.isNotEmpty()) createViolation(paths) else null
    }
}
