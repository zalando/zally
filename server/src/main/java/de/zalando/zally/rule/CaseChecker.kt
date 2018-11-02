package de.zalando.zally.rule

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.getAllHeaders
import de.zalando.zally.util.getAllParameters
import de.zalando.zally.util.getAllProperties
import io.github.config4k.extract

class CaseChecker(
    val cases: Map<String, Regex>,
    val propertyNames: CaseCheck? = null,
    val pathSegments: CaseCheck? = null,
    val pathParameterNames: CaseCheck? = null,
    val queryParameterNames: CaseCheck? = null,
    val headerNames: CaseCheck? = null
) {
    companion object {
        fun load(config: Config): CaseChecker = config.extract("CaseChecker")
    }

    class CaseCheck(
        val allow: List<Regex>
    ) {
        fun accepts(input: String): Boolean = allow.any { it.matches(input) }

        override fun toString(): String = allow.map { it.pattern }.toString()
    }

    fun checkPathSegments(context: Context): List<Violation> = context.api
        .paths?.entries.orEmpty()
        .flatMap { (path, item) ->
            val inputs = path
                .split("/")
                .filterNot { it.isEmpty() }
                .filterNot { PatternUtil.isPathVariable(it) }
            check("Path segment", "Path segments", pathSegments, inputs)
                ?.let { context.violations(it, item) }
                .orEmpty()
        }

    fun checkPropertyNames(context: Context): List<Violation> = context.api
        .getAllProperties()
        .flatMap { (name, schema) ->
            check("Property", "Properties", propertyNames, name)
                ?.let { context.violations(it, schema) }
                .orEmpty()
        }

    fun checkHeadersNames(context: Context): List<Violation> = context.api
        .getAllHeaders()
        .flatMap { header ->
            check("Header", "Headers", headerNames, header.name)
                ?.let { context.violations(it, header.element) }
                .orEmpty()
        }

    fun checkPathParameterNames(context: Context): List<Violation> =
        checkParameterNames(context, "Path", pathParameterNames)

    fun checkQueryParameterNames(context: Context): List<Violation> =
        checkParameterNames(context, "Query", queryParameterNames)

    private fun checkParameterNames(
        context: Context,
        type: String,
        check: CaseCheck?
    ): List<Violation> = context.api
        .getAllParameters().values
        .filter { type.toLowerCase() == it.`in` }
        .flatMap { param ->
            check("$type parameter", "$type parameters", check, param.name)
                ?.let { context.violations(it, param) }
                .orEmpty()
        }

    internal fun check(prefix: String, prefixes: String, check: CaseChecker.CaseCheck?, input: String): String? =
        check(prefix, prefixes, check, listOf(input))

    internal fun check(
        prefix: String,
        prefixes: String,
        check: CaseChecker.CaseCheck?,
        vararg inputs: String
    ): String? = check(prefix, prefixes, check, inputs.asIterable())

    internal fun check(
        prefix: String,
        prefixes: String,
        check: CaseChecker.CaseCheck?,
        inputs: Iterable<String>
    ): String? {
        if (check == null) {
            return null
        }

        val mismatches = inputs.filterNot { check.accepts(it) }
        if (mismatches.isEmpty()) {
            return null
        }

        val message = StringBuilder()
        appendMismatches(message, prefix, prefixes, mismatches)
        appendRegex(message, check)
        appendSuggestions(message, inputs)

        return message.toString()
    }

    private fun appendMismatches(
        message: StringBuilder,
        prefix: String,
        prefixes: String,
        mismatches: List<String>
    ) {
        when {
            mismatches.size == 1 -> message.append(prefix)
            else -> message.append(prefixes)
        }
        message.append(" ")
        message.append(mismatches.joinToString { "'$it'" })
        when {
            mismatches.size == 1 -> message.append(" does not match ")
            else -> message.append(" do not match ")
        }
    }

    private fun appendRegex(message: StringBuilder, check: CaseCheck) {
        if (check.allow.size > 1) {
            message.append("any of ")
        }
        message.append(check.allow.joinToString { it.describe() })
    }

    private fun appendSuggestions(message: StringBuilder, inputs: Iterable<String>) {
        val suggestions = inputs
            .map { input ->
                cases
                    .values
                    .filter { it.matches(input) }
                    .map { it.describe() }
                    .toSet()
            }
            .reduce { acc, set ->
                acc.intersect(set)
            }
            .toList()
        when {
            suggestions.size == 1 -> message.append(" but seems to be ").append(suggestions[0])
            suggestions.isNotEmpty() -> message.append(" but seems to be one of ").append(suggestions.joinToString())
        }
    }

    private fun Regex.describe(): String = cases
        .filterValues { it.pattern == this.pattern }
        .keys
        .firstOrNull()
        ?.let { name -> "$name ('${this.pattern}')" }
        ?: "'${this.pattern}'"
}
