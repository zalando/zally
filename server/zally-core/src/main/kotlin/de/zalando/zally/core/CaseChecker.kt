package de.zalando.zally.core

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.core.util.PatternUtil
import de.zalando.zally.core.util.getAllHeaders
import de.zalando.zally.core.util.getAllParameters
import de.zalando.zally.core.util.getAllProperties
import de.zalando.zally.core.util.getAllSchemas
import io.github.config4k.extract
import io.swagger.v3.oas.models.media.Schema

/**
 * Utility class for checking cases of strings against configured requirements.
 */
@Suppress("TooManyFunctions")
class CaseChecker(
    val cases: Map<String, Regex>,
    val propertyNames: CaseCheck? = null,
    val pathSegments: CaseCheck? = null,
    val pathParameterNames: CaseCheck? = null,
    val queryParameterNames: CaseCheck? = null,
    val headerNames: CaseCheck? = null,
    val tagNames: CaseCheck? = null,
    val discriminatorValues: CaseCheck? = null,
    val enumValues: CaseCheck? = null
) {
    companion object {
        fun load(config: Config): CaseChecker = config.extract("CaseChecker")
    }

    /**
     * Represents the check regex requirements for a specific kind of string.
     */
    class CaseCheck(
        val allow: List<Regex>
    ) {
        /**
         * Apply this check to the supplied input.
         * @param input the string to check.
         * @return true if any allowed regexes match the input.
         */
        fun accepts(input: String): Boolean = allow.any { it.matches(input) }

        override fun toString(): String = allow.map { it.pattern }.toString()
    }

    /**
     * Check that path segments match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
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

    /**
     * Check that property names match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    fun checkPropertyNames(context: Context): List<Violation> = context.api
        .getAllProperties()
        .flatMap { (name, schema) ->
            check("Property", "Properties", propertyNames, name)
                ?.let { context.violations(it, schema) }
                .orEmpty()
        }

    /**
     * Check that discriminator values match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    fun checkDiscriminatorValues(context: Context): List<Violation> = context.api
        .getAllSchemas()
        .filter { schema -> schema.discriminator != null }
        .flatMap { schema ->
            checkDiscriminatorValues(context, schema)
        }

    private fun checkDiscriminatorValues(context: Context, schema: Schema<Any>): List<Violation> = when (schema.type) {
        "object" -> {
            schema.properties?.values?.flatMap { checkDiscriminatorValues(context, it) }.orEmpty() +
                checkDiscriminatorMappingKeyValues(context, schema) +
                checkDiscriminatorPropertyEnumValues(context, schema)
        }
        else -> emptyList()
    }

    private fun checkDiscriminatorPropertyEnumValues(context: Context, schema: Schema<Any>): List<Violation> =
        schema.discriminator?.propertyName
            ?.let { propertyName ->
                schema.properties?.get(propertyName)?.let { property ->
                    val values = property.enum?.map { it.toString() }
                    check("Discriminator property enum value", "Discriminator property enums", discriminatorValues, values)
                        ?.let { context.violations(it, property) }
                }
            }
            .orEmpty()

    private fun checkDiscriminatorMappingKeyValues(context: Context, schema: Schema<Any>): List<Violation> =
        check("Discriminator value", "Discriminator values", discriminatorValues, schema.discriminator?.mapping?.keys)
            ?.let { context.violations(it, schema.discriminator) }
            .orEmpty()

    /**
     * Check that enum values match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    fun checkEnumValues(context: Context): List<Violation> = context.api
        .getAllSchemas()
        .flatMap { schema ->
            checkEnumValues(context, schema)
        }

    private fun checkEnumValues(context: Context, schema: Schema<Any>): List<Violation> = when (schema.type) {
        "string" -> {
            check("Enum value", "Enum values", enumValues, schema.enum?.map { it.toString() })
                ?.let { context.violations(it, schema) }
                .orEmpty()
        }
        "object" -> {
            schema.properties
                ?.filterKeys { key -> key != schema.discriminator?.propertyName }
                ?.values
                ?.flatMap { propertySchema ->
                    checkEnumValues(context, propertySchema)
                }
                .orEmpty()
        }
        else -> emptyList()
    }

    /**
     * Check that header names match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    fun checkHeadersNames(context: Context): List<Violation> = context.api
        .getAllHeaders()
        .flatMap { header ->
            check("Header", "Headers", headerNames, header.name)
                ?.let { context.violations(it, header.element) }
                .orEmpty()
        }

    /**
     * Check that tag names match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    fun checkTagNames(context: Context): List<Violation> =
        checkTagNamesDefined(context) +
            checkTagNamesUsed(context)

    private fun checkTagNamesDefined(context: Context): List<Violation> = context.api
        .tags
        .orEmpty()
        .filter { it != null && it.name != null }
        .flatMap { tag ->
            check("Tag", "Tags", tagNames, tag.name)
                ?.let { context.violations(it, tag) }
                .orEmpty()
        }

    private fun checkTagNamesUsed(context: Context): List<Violation> = context.api
        .paths
        ?.values
        .orEmpty()
        .flatMap { path -> path.readOperations() }
        .flatMap { op ->
            op?.tags.orEmpty().flatMap { name ->
                check("Tag", "Tags", tagNames, name)
                    ?.let { context.violations(it, op.tags) }
                    .orEmpty()
            }
        }

    /**
     * Check that path parameter names match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    fun checkPathParameterNames(context: Context): List<Violation> =
        checkParameterNames(context, "Path", pathParameterNames)

    /**
     * Check that query parameter names match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
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

    internal fun check(prefix: String, prefixes: String, check: CaseCheck?, input: String): String? =
        check(prefix, prefixes, check, listOf(input))

    internal fun check(
        prefix: String,
        prefixes: String,
        check: CaseCheck?,
        vararg inputs: String
    ): String? = check(prefix, prefixes, check, inputs.asIterable())

    internal fun check(
        prefix: String,
        prefixes: String,
        check: CaseCheck?,
        inputs: Iterable<String>?
    ): String? {
        if (check == null || inputs == null) {
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
