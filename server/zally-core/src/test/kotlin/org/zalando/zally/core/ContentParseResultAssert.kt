package org.zalando.zally.core

import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Violation
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

class ContentParseResultAssert<T : Any>(actual: ContentParseResult<T>?) :
    AbstractAssert<ContentParseResultAssert<T>, ContentParseResult<T>?>(actual, ContentParseResultAssert::class.java) {

    fun resultsInSuccess() {
        failIfNot<ContentParseResult.ParsedSuccessfully<T>>()
    }

    fun resultsInNotApplicable() {
        failIfNot<ContentParseResult.NotApplicable<T>>()
    }

    fun resultsInErrors(vararg expectedErrors: Violation) {
        failIfNot<ContentParseResult.ParsedWithErrors<T>>()
        val result = actual as ContentParseResult.ParsedWithErrors<T>
        Assertions.assertThat(result.violations).hasSameElementsAs(expectedErrors.toList())
    }

    private inline fun <reified TExpected> failIfNot() {
        if (actual !is TExpected) {
            val type = TExpected::class.simpleName
            failWithMessage(
                when (actual) {
                    null -> "Expected result to be '$type'. Was null instead."
                    is ContentParseResult.NotApplicable -> "Expected result to be '$type'. Was 'NotApplicable' instead."
                    is ContentParseResult.ParsedWithErrors -> {
                        val sep = "\n  - "
                        val violations = actual.violations.joinToString(sep, sep, "\n")
                        "Expected result to be '$type'. Was 'ParsedWithErrors' instead with those violations: $violations"
                    }
                    is ContentParseResult.ParsedSuccessfully -> "Expected result to be '$type'. Was 'ParsedSuccessfully' instead."
                }
            )
        }
    }

    companion object {
        fun assertThat(actual: ContentParseResult<Context>) = ContentParseResultAssert(actual)
    }
}
