package de.zalando.zally.rule.zally

import de.zalando.zally.getSwaggerContextFromContent
import de.zalando.zally.rule.ZallyAssertions
import org.intellij.lang.annotations.Language
import org.junit.Test

class TagAllOperationsRuleTest {

    private val cut = TagAllOperationsRule()

    @Test
    fun `checkOperationsAreTagged with no operations returns no violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            """.trimIndent()
        )

        val violations = cut.checkOperationsAreTagged(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkOperationsAreTagged with well tagged operations returns no violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              '/things':
                post:
                  tags:
                    - Things
                  responses:
                    200:
                      description: Done
            """.trimIndent()
        )

        val violations = cut.checkOperationsAreTagged(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkOperationsAreTagged with untagged operations returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              '/things':
                post:
                  responses:
                    200:
                      description: Done
            """.trimIndent()
        )

        val violations = cut.checkOperationsAreTagged(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/paths/~1things/post")
            .descriptionsEqualTo("Operation has no tag")
    }
}
