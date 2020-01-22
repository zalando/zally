package de.zalando.zally.ruleset.zally

import de.zalando.zally.core.DefaultContextFactory
import de.zalando.zally.test.ZallyAssertions
import org.intellij.lang.annotations.Language
import org.junit.Test

class ExtractBasePathRuleTest {

    private val rule = ExtractBasePathRule()

    @Test
    fun `validate swagger with no paths returns no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .isEmpty()
    }

    @Test
    fun `validate swagger with no common first segments returns no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            paths:
              /orders/{order_id}: {}
              /orders/{updates}: {}
              /merchants: {}
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .isEmpty()
    }

    @Test
    fun `validate swagger with single path returns no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            paths:
              /orders/{order_id}: {}
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .isEmpty()
    }

    @Test
    fun `validate swagger with common first segment returns violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            paths:
              /shipment/{shipment_id}: {}
              /shipment/{shipment_id}/status: {}
              /shipment/{shipment_id}/details: {}
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .descriptionsEqualTo("All paths start with prefix '/shipment' which could be part of basePath.")
            .pointersEqualTo("/paths")
    }

    @Test
    fun `validate swagger with multiple common first segments returns violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            paths:
              /queue/models/configs/{config-id}: {}
              /queue/models/: {}
              /queue/models/{model-id}: {}
              /queue/models/summaries: {}
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .descriptionsEqualTo("All paths start with prefix '/queue/models' which could be part of basePath.")
            .pointersEqualTo("/paths")
    }

    @Test
    fun `validate swagger with common prefix but no common first segments returns no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            paths:
              /api/{api_id}/deployments: {}
              /api/{api_id}/: {}
              /applications/{app_id}: {}
              /applications/: {}
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .isEmpty()
    }

    @Test
    fun `validate openapi with common first segment returns violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
            paths:
              /shipment/{shipment_id}: {}
              /shipment/{shipment_id}/status: {}
              /shipment/{shipment_id}/details: {}
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .descriptionsEqualTo("All paths start with prefix '/shipment' which could be part of servers' urls.")
            .pointersEqualTo("/paths")
    }
}
