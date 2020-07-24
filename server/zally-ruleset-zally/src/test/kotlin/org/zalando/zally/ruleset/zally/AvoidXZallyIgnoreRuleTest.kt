package org.zalando.zally.ruleset.zally

import org.zalando.zally.core.ObjectTreeReader
import org.zalando.zally.test.ZallyAssertions
import org.intellij.lang.annotations.Language
import org.junit.Test

class AvoidXZallyIgnoreRuleTest {

    private val rule = AvoidXZallyIgnoreRule()
    private val reader = ObjectTreeReader()

    @Test
    fun `validate swagger with inline ignores returns violation`() {
        @Language("YAML")
        val root = reader.read(
            """
            swagger: 2.0
            x-zally-ignore: [ ONE, TWO, THREE]
            """.trimIndent()
        )

        val violations = rule.validate(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsEqualTo("Ignores rules ONE, TWO, THREE")
            .pointersEqualTo("/x-zally-ignore")
    }

    @Test
    fun `validate swagger with dashed ignores returns violation`() {
        @Language("YAML")
        val root = reader.read(
            """
            swagger: 2.0
            x-zally-ignore:
              - ONE
              - TWO
              - THREE
            """.trimIndent()
        )

        val violations = rule.validate(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsEqualTo("Ignores rules ONE, TWO, THREE")
            .pointersEqualTo("/x-zally-ignore")
    }

    @Test
    fun `validate swagger with ignores within object returns violation`() {
        @Language("YAML")
        val root = reader.read(
            """
            swagger: 2.0
            info:
              x-zally-ignore: [ ONE, TWO, THREE]
            """.trimIndent()
        )

        val violations = rule.validate(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsEqualTo("Ignores rules ONE, TWO, THREE")
            .pointersEqualTo("/info/x-zally-ignore")
    }

    @Test
    fun `validate openapi with ignores within array returns violation`() {
        @Language("YAML")
        val root = reader.read(
            """
            openapi: 3.0.0
            servers:
              - url: http://example.com
                x-zally-ignore: [ONE, TWO, THREE]
            """.trimIndent()
        )

        val violations = rule.validate(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsEqualTo("Ignores rules ONE, TWO, THREE")
            .pointersEqualTo("/servers/0/x-zally-ignore")
    }

    @Test
    fun `validate openapi with invalid ignores string returns violation`() {
        @Language("YAML")
        val root = reader.read(
            """
            swagger: 2.0
            x-zally-ignore: INVALID
            """.trimIndent()
        )

        val violations = rule.validate(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsEqualTo("Invalid ignores, expected list but found single value \"INVALID\"")
            .pointersEqualTo("/x-zally-ignore")
    }

    @Test
    fun `validate openapi with invalid ignores object returns violation`() {
        @Language("YAML")
        val root = reader.read(
            """
            swagger: 2.0
            x-zally-ignore:
              invalid: INVALID
            """.trimIndent()
        )

        val violations = rule.validate(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsEqualTo("Invalid ignores, expected list but found {\"invalid\":\"INVALID\"}")
            .pointersEqualTo("/x-zally-ignore")
    }
}
