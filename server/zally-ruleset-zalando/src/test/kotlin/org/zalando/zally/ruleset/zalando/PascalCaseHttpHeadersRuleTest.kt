package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.rulesConfig
import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class PascalCaseHttpHeadersRuleTest {

    private val rule = PascalCaseHttpHeadersRule(rulesConfig)

    @Test
    fun `checkHttpHeaders should return violation if not pascal case is used`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            components:
              headers:
                not-pascal-case-header: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkHttpHeaders(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).isEqualTo("Header has to be Hyphenated-Pascal-Case")
        assertThat(violations[0].pointer.toString()).isEqualTo("/components/headers/not-pascal-case-header")
    }

    @Test
    fun `checkHttpHeaders should return no violation if all headers are pascal case`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            components:
              headers:
                Hyphenated-Pascal-Case-Header: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkHttpHeaders(context)

        assertThat(violations).isEmpty()
    }
}
