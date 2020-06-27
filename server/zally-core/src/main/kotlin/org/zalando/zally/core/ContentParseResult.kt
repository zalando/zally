package org.zalando.zally.core

import org.zalando.zally.rule.api.Violation

/**
 * Possible results of the `parse` operation.
 */
@Suppress("unused") // type parameter RootT used at sealed class level to simplify `when` blocks
sealed class ContentParseResult<out RootT : Any> {

    /**
     * The content was not applicable.
     * Example: not an OpenAPI specification.
     */
    class NotApplicable<RootT : Any> : ContentParseResult<RootT>()

    /**
     * The content was of a recognised type, but some results (violations) are already
     * available after parsing.
     * Example: some required parts of an OpenAPI specification are missing.
     */
    data class ParsedWithErrors<RootT : Any>(val violations: List<Violation>) : ContentParseResult<RootT>()

    /**
     * The content was of a recognised type and the parsing was successful.
     */
    data class ParsedSuccessfully<RootT : Any>(val result: RootT) : ContentParseResult<RootT>()

    inline fun <reified T : Any> of(): ContentParseResult<T> = when (this) {
        is NotApplicable -> NotApplicable()
        is ParsedWithErrors -> ParsedWithErrors(violations)
        is ParsedSuccessfully -> {
            val resultT = result as? T
            if (resultT == null) throw IllegalStateException("Cannot change the type of a ParsedSuccessfully")
            else ParsedSuccessfully(resultT)
        }
    }
}
