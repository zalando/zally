package org.zalando.zally.ruleset.zalando

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.test.ZallyAssertions.assertThat

@Suppress("StringLiteralDuplication", "UndocumentedPublicClass", "UnsafeCallOnNullableType")
class AvoidTrailingSlashesRuleTest {

    private val rule = AvoidTrailingSlashesRule()

    @Test
    fun emptySwagger() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            """.trimIndent()
        )

        val violations = rule.validate(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun positiveCase() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            paths:
              /: {}
              /api/test-api: {}
            """.trimIndent()
        )

        val violations = rule.validate(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun negativeCase() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            paths:
              /api/test-api/: {}
              /api//test-api: {}
              /api/test: {}
              /some/other/path: {}
              /long/bad/path/with/slash/: {}
            """.trimIndent()
        )

        val violations = rule.validate(context)

        assertThat(violations)
            .descriptionsAllEqualTo("Rule avoid trailing slashes is not followed")
            .pointersEqualTo("/paths/~1api~1test-api~1", "/paths/~1api~1~1test-api", "/paths/~1long~1bad~1path~1with~1slash~1")
            .hasSize(3)
    }
}
