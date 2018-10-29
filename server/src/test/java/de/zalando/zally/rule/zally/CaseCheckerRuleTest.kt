package de.zalando.zally.rule.zally

import de.zalando.zally.getSwaggerContextFromContent
import de.zalando.zally.rule.ZallyAssertions
import de.zalando.zally.testConfig
import org.intellij.lang.annotations.Language
import org.junit.Test

class CaseCheckerRuleTest {

    private val cut = CaseCheckerRule(testConfig)

    @Test
    fun `checkPropertyNames returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            definitions:
              Defined:
                properties:
                  InVaLiD!:
                    type: boolean
            """.trimIndent()
        )

        val violations = cut.checkPropertyNames(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/definitions/Defined/properties/InVaLiD!")
            .descriptionsAllMatch("Property 'InVaLiD!' does not match .*".toRegex())
    }

    @Test
    fun `checkPathParameterNames returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              /things:
                post:
                  parameters:
                  - in: path
                    name: InVaLiD!
            """.trimIndent()
        )

        val violations = cut.checkPathParameterNames(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Path parameter 'InVaLiD!' does not match .*".toRegex())
            .pointersEqualTo("/paths/~1things/post/parameters/0")
    }

    @Test
    fun `checkQueryParameterNames returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              /things:
                post:
                  parameters:
                  - in: query
                    name: InVaLiD!
            """.trimIndent()
        )

        val violations = cut.checkQueryParameterNames(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Query parameter 'InVaLiD!' does not match .*".toRegex())
            .pointersEqualTo("/paths/~1things/post/parameters/0")
    }

    @Test
    fun `checkHeaderNames returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              /things:
                post:
                  parameters:
                  - in: header
                    name: InVaLiD!
            """.trimIndent()
        )

        val violations = cut.checkHeaderNames(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Header 'InVaLiD!' does not match .*".toRegex())
            .pointersEqualTo("/paths/~1things/post/parameters/0")
    }

    @Test
    fun `checkPathSegments returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              /things/{param}//InVaLiD:
                post:
            """.trimIndent()
        )

        val violations = cut.checkPathSegments(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Path segment 'InVaLiD' does not match .*".toRegex())
            .pointersEqualTo("/paths/~1things~1{param}~1~1InVaLiD")
    }
}
