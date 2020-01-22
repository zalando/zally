package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class NestedPathsMayBeRootPathsRuleTest {

    private val rule = NestedPathsMayBeRootPathsRule()

    @Test
    fun `checkNestedPaths should return violations for paths containing nested sub resources`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              "/countries/{country-id}/populated/cities/{city-id}": {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkNestedPaths(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).contains("may be top-level resource")
        assertThat(violations[0].pointer.toString())
            .isEqualTo("/paths/~1countries~1{country-id}~1populated~1cities~1{city-id}")
    }

    @Test
    fun `checkNestedPaths should return no violations if there are no paths containing nested sub resources`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /pets/dogs: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkNestedPaths(context)

        assertThat(violations).isEmpty()
    }
}
