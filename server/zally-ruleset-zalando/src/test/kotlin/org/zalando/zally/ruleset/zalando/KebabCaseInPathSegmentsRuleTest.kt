package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.rulesConfig
import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class KebabCaseInPathSegmentsRuleTest {

    private val rule = KebabCaseInPathSegmentsRule(rulesConfig)

    @Test
    fun `checkKebabCaseInPathSegments should return violation for path segments which are not lowercase separate words with hyphens`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /partnerOrders: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkKebabCaseInPathSegments(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).contains("Use lowercase separate words with hyphens")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1partnerOrders")
    }

    @Test
    fun `checkKebabCaseInPathSegments should return violation for sub resource names which are not lowercase separate words with hyphens`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /partner-orders/{order-id}/orderItems: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkKebabCaseInPathSegments(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).contains("Use lowercase separate words with hyphens")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1partner-orders~1{order-id}~1orderItems")
    }

    @Test
    fun `checkKebabCaseInPathSegments should return no violation if all segments are lowercase separated words with hyphens`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /partner-orders/{orderId}/order-items: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkKebabCaseInPathSegments(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `lowerCaseHyphenSeparatedRegex should match lowercase, with hyphen separated words`() {
        assertThat("articles".matches(rule.lowerCaseHyphenSeparatedRegex)).isTrue()
        assertThat("partner-articles".matches(rule.lowerCaseHyphenSeparatedRegex)).isTrue()

        assertThat("COOL-ARTICLES".matches(rule.lowerCaseHyphenSeparatedRegex)).isFalse()
        assertThat("wEirDARtiCles".matches(rule.lowerCaseHyphenSeparatedRegex)).isFalse()
    }
}
