package org.zalando.zally.ruleset.zally

import org.zalando.zally.test.ZallyAssertions
import org.zalando.zally.core.rulesConfig
import org.zalando.zally.core.DefaultContextFactory
import org.intellij.lang.annotations.Language
import org.junit.Assume.assumeTrue
import org.junit.Test

class StringPropertyLengthBoundsRuleTest {

    private val cut = StringPropertyLengthBoundsRule(rulesConfig)

    @Test
    fun `checkStringLengthBounds with bounded string length returns no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.2
            info:
              title: Thing API
              version: 1.0.0
            components:
              schemas:
                Thing:
                  type: object
                  properties:
                    theString:
                      type: string
                      minLength: 0
                      maxLength: 10
            """.trimIndent()
        )

        val violations = cut.checkStringLengthBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkStringLengthBounds with left-bounded string length returns max violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.2
            info:
              title: Thing API
              version: 1.0.0
            components:
              schemas:
                Thing:
                  type: object
                  properties:
                    theString:
                      type: string
                      minLength: 0
            """.trimIndent()
        )

        val violations = cut.checkStringLengthBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/components/schemas/Thing/properties/theString")
            .descriptionsEqualTo("No maxLength defined")
    }

    @Test
    fun `checkStringLengthBounds with right-bounded string length returns min violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.2
            info:
              title: Thing API
              version: 1.0.0
            components:
              schemas:
                Thing:
                  type: object
                  properties:
                    theString:
                      type: string
                      maxLength: 10
            """.trimIndent()
        )

        val violations = cut.checkStringLengthBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/components/schemas/Thing/properties/theString")
            .descriptionsEqualTo("No minLength defined")
    }

    @Test
    fun `checkStringLengthBounds with unbounded string length returns min and max violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.2
            info:
              title: Thing API
              version: 1.0.0
            components:
              schemas:
                Thing:
                  type: object
                  properties:
                    theString:
                      type: string
            """.trimIndent()
        )

        val violations = cut.checkStringLengthBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/components/schemas/Thing/properties/theString", "/components/schemas/Thing/properties/theString")
            .descriptionsEqualTo("No minLength defined", "No maxLength defined")
    }

    @Test
    fun `checkStringLengthBounds with negative bounded string length returns min and max violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.2
            info:
              title: Thing API
              version: 1.0.0
            components:
              schemas:
                Thing:
                  type: object
                  properties:
                    theString:
                      type: string
                      minLength: -100
                      maxLength: -50
            """.trimIndent()
        )

        val violations = cut.checkStringLengthBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/components/schemas/Thing/properties/theString", "/components/schemas/Thing/properties/theString")
            .descriptionsEqualTo("Negative minLength is invalid", "Negative maxLength is invalid")
    }

    @Test
    fun `checkStringLengthBounds with reversed bounded string length returns min and max violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.2
            info:
              title: Thing API
              version: 1.0.0
            components:
              schemas:
                Thing:
                  type: object
                  properties:
                    theString:
                      type: string
                      minLength: 50
                      maxLength: 10
            """.trimIndent()
        )

        val violations = cut.checkStringLengthBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/components/schemas/Thing/properties/theString")
            .descriptionsEqualTo("minLength > maxLength is invalid")
    }

    @Test
    fun `checkStringLengthBounds with non-whitelisted format returns min and max violation`() {
        val format = "password"

        assumeTrue(
            "test assumes config StringPropertyLengthBoundsRule.formatWhitelist excludes '$format'",
            format !in cut.formatWhitelist
        )

        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.2
            info:
              title: Thing API
              version: 1.0.0
            components:
              schemas:
                Thing:
                  type: object
                  properties:
                    theString:
                      type: string
                      format: $format
            """.trimIndent()
        )

        val violations = cut.checkStringLengthBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/components/schemas/Thing/properties/theString", "/components/schemas/Thing/properties/theString")
            .descriptionsEqualTo("No minLength defined", "No maxLength defined")
    }

    @Test
    fun `checkStringLengthBounds with whitelisted format returns no violations`() {
        val format = "date-time"

        assumeTrue(
            "test assumes config StringPropertyLengthBoundsRule.formatWhitelist includes '$format'",
            format in cut.formatWhitelist
        )

        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.2
            info:
              title: Thing API
              version: 1.0.0
            components:
              schemas:
                Thing:
                  type: object
                  properties:
                    theString:
                      type: string
                      format: $format
            """.trimIndent()
        )

        val violations = cut.checkStringLengthBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkStringLengthBounds with pattern returns no violations`() {
        assumeTrue(
            "Test assumes config StringPropertyLengthBoundsRule.patternImpliesLimits is 'true'",
            cut.patternImpliesLimits
        )

        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.2
            info:
              title: Thing API
              version: 1.0.0
            components:
              schemas:
                Thing:
                  type: object
                  properties:
                    theString:
                      type: string
                      pattern: #([a-f0-9]{6}|[a-f0-9]{3})
            """.trimIndent()
        )

        val violations = cut.checkStringLengthBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkStringLengthBounds with enum returns no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.2
            info:
              title: Thing API
              version: 1.0.0
            components:
              schemas:
                Thing:
                  type: object
                  properties:
                    theString:
                      type: string
                      enum: [foo, bar]
            """.trimIndent()
        )

        val violations = cut.checkStringLengthBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }
}
