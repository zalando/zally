package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.test.ZallyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class PluralizeNamesForArraysRuleTest {

    private val rule = PluralizeNamesForArraysRule()

    @Test
    fun `checkArrayPropertyNamesArePlural should return violations for array property names which are not pluralized`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            components:
              schemas:
                car:
                  properties:
                    feature: # name is not pluralized
                      type: array
                      items:
                        type: string
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkArrayPropertyNamesArePlural(context)

        ZallyAssertions
            .assertThat(violations)
            .isNotEmpty
            .descriptionsAllEqualTo("Array property name appears to be singular: feature")
            .pointersEqualTo("/components/schemas/car/properties/feature")
    }

    @Test
    fun `checkArrayPropertyNamesArePlural should not return violations for array property names which are pluralized`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            components:
              schemas:
                car:
                  properties:
                    features: # name is pluralized
                      type: array
                      items:
                        type: string
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkArrayPropertyNamesArePlural(context)

        assertThat(violations).isEmpty()
    }
}
