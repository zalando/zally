package de.zalando.zally.rule

import de.zalando.zally.rule.api.Violation

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
    data class Success<RootT : Any>(val result: RootT) : ContentParseResult<RootT>()

    /**
     * Transforms a [Success] of [RootT] into another [ContentParseResult], of type [T].
     * Any other value will be converted an equivalent [ContentParseResult] of type [T].
     */
    fun <T : Any> flatMap(f: (RootT) -> ContentParseResult<T>): ContentParseResult<T> = when (this) {
        is ContentParseResult.NotApplicable -> ContentParseResult.NotApplicable()
        is ContentParseResult.ParsedWithErrors -> ContentParseResult.ParsedWithErrors(violations)
        is ContentParseResult.Success -> f(this.result)
    }

    /**
     * Transforms a [Success] of [RootT] into another [Success], or type [T].
     * Any other value will be converted an equivalent [ContentParseResult] of type [T].
     */
    fun <T : Any> map(f: (RootT) -> T): ContentParseResult<T> = flatMap { Success(f(it)) }
}
