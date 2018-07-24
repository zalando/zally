package de.zalando.zally.rule

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse

// todo: When all rules deriving from `HttpHeadersRule` are converted to the `Context` object, replace `HttpHeadersRule` with this class (#714).
abstract class HttpHeadersRuleWithContext(rulesConfig: Config) {

    private val headersWhitelist = rulesConfig.getStringList(HttpHeadersRule::class.simpleName + ".whitelist").toSet()

    abstract fun createViolation(context: Context, header: HeaderElement): Violation

    abstract fun isViolation(header: HeaderElement): Boolean

    data class HeaderElement(
        val name: String,
        val element: Any
    )

    open fun validate(context: Context): List<Violation> {

        fun Collection<Parameter>?.extractHeaders() = orEmpty()
            .filter { it.`in` == "header" }
            .map { HeaderElement(it.name, it) }
            .toSet()

        fun Collection<ApiResponse>?.extractHeaders() = orEmpty()
            .flatMap { it.headers.orEmpty().entries }
            .map { HeaderElement(it.key, it.value) }
            .toSet()

        val fromParams = context.api.components.parameters.orEmpty().values.extractHeaders()

        val fromPaths = context.api.paths.orEmpty().flatMap { (_, path) ->
            val fromPathParameters = path.parameters.extractHeaders()
            val fromOperations = path.readOperations().flatMap { operation ->
                val fromOpParams = operation.parameters.extractHeaders()
                val fromOpResponses = operation.responses.orEmpty().values.extractHeaders()
                fromOpParams + fromOpResponses
            }
            fromPathParameters + fromOperations
        }

        val allHeaders = fromParams + fromPaths
        return allHeaders
            .filter { it.name !in headersWhitelist && isViolation(it) }
            .map { createViolation(context, it) }
    }
}
