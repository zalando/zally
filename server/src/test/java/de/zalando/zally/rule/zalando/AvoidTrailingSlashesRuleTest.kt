package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.DefaultContext
import de.zalando.zally.rule.ZallyAssertions.Companion.assertThat
import org.junit.Test

@Suppress("StringLiteralDuplication", "UndocumentedPublicClass", "UnsafeCallOnNullableType")
class AvoidTrailingSlashesRuleTest {

    private val rule = AvoidTrailingSlashesRule()

    @Test
    fun emptySwagger() {
        val context = DefaultContext.createOpenApiContext("""
            openapi: '3.0.0'
            """.trimIndent())!!

        val violations = rule.validate(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun positiveCase() {
        val context = DefaultContext.createOpenApiContext("""
            openapi: '3.0.0'
            paths:
              /api/test-api: {}
            """.trimIndent())!!

        val violations = rule.validate(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun negativeCase() {
        val context = DefaultContext.createOpenApiContext("""
            openapi: '3.0.0'
            paths:
              /api/test-api/: {}
              /api/test: {}
              /some/other/path: {}
              /long/bad/path/with/slash/: {}
            """.trimIndent())!!

        val violations = rule.validate(context)

        assertThat(violations)
            .descriptionsAllEqualTo("Rule avoid trailing slashes is not followed")
            .pointersEqualTo("/paths/~1api~1test-api~1", "/paths/~1long~1bad~1path~1with~1slash~1")
            .hasSize(2)
    }
}
