package de.zalando.zally.rule

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.getAllProperties
import io.github.config4k.extract

class CaseChecker(
    val cases: Map<String, Regex>,
    val propertyNames: CaseCheck?
) {
    companion object {
        fun load(config: Config): CaseChecker = config.extract("CaseChecker")
    }

    class CaseCheck(
        private val regex: Regex,
        private val whitelist: List<Regex>?
    ) {
        fun accepts(input: String): Boolean =
            (whitelist.orEmpty() + regex).any { it.matches(input) }

        override fun toString(): String {
            return regex.pattern
        }
    }

    fun checkPropertyNames(context: Context): List<Violation> {
        return context.api
            .getAllProperties()
            .flatMap { (name, param) ->
                check("Property", "Properties", propertyNames, name)
                    ?.let { context.violations(it, param) }
                    .orEmpty()
            }
    }

    fun check(prefix: String, prefixes: String, check: CaseChecker.CaseCheck?, vararg inputs: String): String? {
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
        val caseMatches = cases.filterValues { it.pattern == check.toString() }
        if (caseMatches.isEmpty()) {
            message.append("regex $check")
        } else {
            val entry = caseMatches.iterator().next()
            message.append("${entry.key} (${entry.value})")
        }
    }

    private fun appendSuggestions(message: StringBuilder, inputs: Array<out String>) {
        val suggestions = inputs.asIterable()
            .map { input ->
                cases.filterValues { it.matches(input) }.keys
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

    private fun caseToString(case: CaseChecker.CaseCheck): String {
        val matches = cases.filterValues { it.pattern == case.toString() }
        return if (matches.isEmpty()) {
            "regex $case"
        } else {
            val entry = matches.iterator().next()
            "${entry.key} (${entry.value})"
        }
    }
}
