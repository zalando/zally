package de.zalando.zally.rule.zally

import de.zalando.zally.getSwaggerContextFromContent
import de.zalando.zally.rule.ZallyAssertions
import org.intellij.lang.annotations.Language
import org.junit.Test

class AtMostOneBodyParameterRuleTest {

    private val cut = AtMostOneBodyParameterRule()

    @Test
    fun withEmptyReturnsEmpty() {
        @Language("YAML")
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            """.trimIndent()

        val context = getSwaggerContextFromContent(yaml)

        val violations = cut.validate(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun withOneBodyParameterReturnsEmpty() {
        @Language("YAML")
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                post:
                  parameters:
                    - in: body
                      name: thing
                      required: true
                      schema:
                        type: string
                  responses:
                    200:
                      description: Done
            """.trimIndent()

        val context = getSwaggerContextFromContent(yaml)

        val violations = cut.validate(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun withTwoBodyParameterReturnsViolations() {
        @Language("YAML")
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                post:
                  parameters:
                    - in: body
                      name: thing
                      required: true
                      schema:
                        type: string
                    - in: body
                      name: anotherThing
                      required: true
                      schema:
                        type: string
                  responses:
                    200:
                      description: Done
            """.trimIndent()

        val context = getSwaggerContextFromContent(yaml)

        val violations = cut.validate(context)

        ZallyAssertions.assertThat(violations)
            .descriptionsAllEqualTo("There can only be one body parameter")
            .pointersEqualTo("/paths/~1things/post/parameters/0", "/paths/~1things/post/parameters/1")
    }
}
