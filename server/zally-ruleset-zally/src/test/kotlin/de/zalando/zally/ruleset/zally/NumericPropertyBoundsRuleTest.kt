package de.zalando.zally.ruleset.zally

import de.zalando.zally.test.ZallyAssertions
import de.zalando.zally.core.DefaultContextFactory
import org.intellij.lang.annotations.Language
import org.junit.Test

class NumericPropertyBoundsRuleTest {

    private val cut = NumericPropertyBoundsRule()

    @Test
    fun `checkNumericBounds with bounded integer returns no violations`() {
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
                    theNumber:
                      type: integer
                      format: int32
                      minimum: 0
                      maximum: 10
            """.trimIndent()
        )

        val violations = cut.checkNumericBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkNumericBounds with left-bounded integer returns max violation`() {
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
                    theNumber:
                      type: integer
                      format: int32
                      minimum: 0
            """.trimIndent()
        )

        val violations = cut.checkNumericBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/components/schemas/Thing/properties/theNumber")
            .descriptionsEqualTo("No maximum defined")
    }

    @Test
    fun `checkNumericBounds with right-bounded integer returns min violation`() {
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
                    theNumber:
                      type: integer
                      format: int32
                      maximum: 0
            """.trimIndent()
        )

        val violations = cut.checkNumericBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/components/schemas/Thing/properties/theNumber")
            .descriptionsEqualTo("No minimum defined")
    }

    @Test
    fun `checkNumericBounds with unbounded integer returns min and max violations`() {
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
                    theNumber:
                      type: integer
                      format: int32
            """.trimIndent()
        )

        val violations = cut.checkNumericBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersAllEqualTo("/components/schemas/Thing/properties/theNumber")
            .descriptionsEqualTo("No minimum defined", "No maximum defined")
    }

    @Test
    fun `checkNumericBounds with unbounded number returns min and max violations`() {
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
                    theNumber:
                      type: number
                      format: float
            """.trimIndent()
        )

        val violations = cut.checkNumericBounds(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersAllEqualTo("/components/schemas/Thing/properties/theNumber")
            .descriptionsEqualTo("No minimum defined", "No maximum defined")
    }
}
