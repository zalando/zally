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
        val context = getSwaggerContextFromContent("""
            swagger: '2.0'
            definitions:
              Defined:
                properties:
                  iNvAlId:
                    type: boolean
            """.trimIndent())

        val violations = cut.checkPropertyNames(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/definitions/Defined/properties/iNvAlId")
            .descriptionsAllMatch("Property 'iNvAlId' does not match .*".toRegex())
    }

    @Test
    fun `checkQueryParameterNames returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent("""
            swagger: '2.0'
            paths:
              /things:
                post:
                  parameters:
                  - in: query
                    name: iNvAlId
            """.trimIndent())

        val violations = cut.checkQueryParameterNames(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Query parameter 'iNvAlId' does not match .*".toRegex())
            .pointersEqualTo("/paths/~1things/post/parameters/0")
    }
}