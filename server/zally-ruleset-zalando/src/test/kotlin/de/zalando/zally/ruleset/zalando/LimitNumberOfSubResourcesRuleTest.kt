package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.rulesConfig
import de.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class LimitNumberOfSubResourcesRuleTest {

    private val rule = LimitNumberOfSubResourcesRule(rulesConfig)

    @Test
    fun `checkNumberOfSubResources should return violation if number of sub resources exceeds the limit`() {
        @Language("YAML")
        val spec = """
              openapi: "3.0.0"
              paths:
                /worlds/{world-id}/countries/{country-id}/states/{state-id}/cities/{city-id}/streets/{street-id}: {}
              """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkNumberOfSubResources(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern("Number of sub-resources should not exceed")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1worlds~1{world-id}~1countries~1{country-id}~1states~1{state-id}~1cities~1{city-id}~1streets~1{street-id}")
    }

    @Test
    fun `checkNumberOfSubResources should return no violation if number of sub resources doesn not exceed the limit`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /articles: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkNumberOfSubResources(context)

        assertThat(violations).isEmpty()
    }
}
