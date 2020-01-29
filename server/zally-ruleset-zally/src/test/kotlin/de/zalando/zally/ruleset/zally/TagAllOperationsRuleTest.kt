package de.zalando.zally.ruleset.zally

import de.zalando.zally.test.ZallyAssertions
import de.zalando.zally.core.DefaultContextFactory
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Violation
import org.intellij.lang.annotations.Language
import org.junit.Test

class TagAllOperationsRuleTest {

    private val cut = TagAllOperationsRule()

    private fun TagAllOperationsRule.checkAll(context: Context): List<Violation> =
        checkOperationsAreTagged(context) +
            checkOperationTagsAreDefined(context) +
            checkDefinedTagsAreUsed(context) +
            checkDefinedTagsAreNamed(context) +
            checkDefinedTagsAreDescribed(context)

    @Test
    fun `checkAll with 'noop' spec returns no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: '2.0'
            """.trimIndent()
        )

        val violations = cut.checkAll(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkAll with 'perfect' spec returns no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: '2.0'
            tags:
              - name: Things
                description: Operations dealing with Things
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

        val violations = cut.checkAll(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkOperationsAreTagged with untagged operations returns violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
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

    @Test
    fun `checkOperationTagsAreDefined with undefined tag returns violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
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

        val violations = cut.checkOperationTagsAreDefined(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/paths/~1things/post")
            .descriptionsEqualTo("Tag 'Things' is not defined")
    }

    @Test
    fun `checkDefinedTagsAreUsed with unused tag returns violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: '2.0'
            tags:
              - name: Things
            """.trimIndent()
        )

        val violations = cut.checkDefinedTagsAreUsed(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/tags/0")
            .descriptionsEqualTo("Tag 'Things' is not used")
    }

    @Test
    fun `checkDefinedTagsAreDescribed without tag description returns violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: '2.0'
            tags:
              - name: Things
            """.trimIndent()
        )

        val violations = cut.checkDefinedTagsAreDescribed(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/tags/0")
            .descriptionsEqualTo("Tag 'Things' has no description")
    }

    @Test
    fun `checkDefinedTagsAreNamed without tag name returns violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: '2.0'
            tags:
              - description: Operations dealing with Things
            """.trimIndent()
        )

        val violations = cut.checkDefinedTagsAreNamed(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/tags/0")
            .descriptionsEqualTo("Tag has no name")
    }
}
