package de.zalando.zally.rule

/**
 * Possible results of the `parse` operation.
 */
@Suppress("unused") // type parameter RootT used at sealed class level to simplify `when` blocks
sealed class ContentParseResult<RootT : Any> {
    /**
     * The content was not applicable.
     * Example: not an OpenAPI specification.
     */
    class NotApplicable<RootT : Any> : ContentParseResult<RootT>()

    /**
     * The content was of a recognised type, but some results (errors) are already
     * available after parsing.
     * Example: some required parts of an OpenAPI specification are missing.
     */
    data class ParsedWithErrors<RootT : Any>(val errors: List<String>) : ContentParseResult<RootT>()

    /**
     * The content was of a recognised type and the parsing was successful.
     */
    data class Success<RootT : Any>(val root: RootT) : ContentParseResult<RootT>()
}
