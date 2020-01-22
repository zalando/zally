package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.DefaultContext
import de.zalando.zally.core.DefaultContextFactory
import de.zalando.zally.core.toJsonPointer
import de.zalando.zally.rule.api.Violation
import io.swagger.parser.util.ClasspathHelper.loadFileFromClasspath
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class MediaTypesRuleTest {

    @Test
    fun `isStandardJsonMediaType for valid input`() {
        assertThat(rule.isStandardJsonMediaType("application/json")).isTrue()
        assertThat(rule.isStandardJsonMediaType("application/problem+json")).isTrue()
        assertThat(rule.isStandardJsonMediaType("application/json-patch+json")).isTrue()
        assertThat(rule.isStandardJsonMediaType("application/merge-patch+json")).isTrue()
    }

    @Test
    fun `isStandardJsonMediaType for invalid input`() {
        assertThat(rule.isStandardJsonMediaType("application/vnd.api+json")).isFalse()
        assertThat(rule.isStandardJsonMediaType("application/x.zalando.contract+json")).isFalse()
    }

    @Test
    fun `isVersionedMediaType for valid input`() {
        assertThat(rule.isVersionedMediaType("application/vnd.api+json;v=12")).isTrue()
        assertThat(rule.isVersionedMediaType("application/x.zalando.contract+json;v=34")).isTrue()
        assertThat(rule.isVersionedMediaType("application/vnd.api+json;version=123")).isTrue()
        assertThat(rule.isVersionedMediaType("application/x.zalando.contract+json;version=345")).isTrue()
    }

    @Test
    fun `isVersionedMediaType for invalid input`() {
        assertThat(rule.isVersionedMediaType("application/vnd.api+json")).isFalse()
        assertThat(rule.isVersionedMediaType("application/x.zalando.contract+json")).isFalse()
        assertThat(rule.isVersionedMediaType("application/vnd.api+json;ver=1")).isFalse()
        assertThat(rule.isVersionedMediaType("application/x.zalando.contract+json;v:1")).isFalse()
        assertThat(rule.isVersionedMediaType("application/vnd.api+json;version=")).isFalse()
        assertThat(rule.isVersionedMediaType("application/x.zalando.contract+json;")).isFalse()
    }

    @Test
    fun `empty specification causes no violation`() {
        val context = DefaultContext("", OpenAPI())
        assertThat(rule.validate(context)).isEmpty()
    }

    @Test
    fun `versioned custom media type causes no violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.0
            paths:
              "/shipment-order/{shipment_order_id}":
                get:
                  responses:
                    200:
                      content:
                        "application/x.zalando.contract+json;v=123": {}
                        "application/vnd.api+json;version=3": {}
        """.trimIndent()
        )
        assertThat(rule.validate(context)).isEmpty()
    }

    @Test
    fun `custom media type without versioning causes violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.0
            paths:
              "/shipment-order/{shipment_order_id}":
                get:
                  responses:
                    200:
                      content:
                        "application/json": {}
                        "application/vnd.api+json": {}
        """.trimIndent()
        )
        assertThat(rule.validate(context)).hasSameElementsAs(
            listOf(
                v("/paths/~1shipment-order~1{shipment_order_id}/get/responses/200/content/application~1vnd.api+json")
            )
        )
    }

    @Test
    fun `only some of multiple paths without versioning causes violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.0
            paths:
              "/path1":
                get:
                  responses:
                    200:
                      content:
                        "application/json": {}
                        "application/vnd.api+json": {}
              "/path2":
                get:
                  responses:
                    200:
                      content:
                        "application/x.zalando.contract+json": {}
              "/path3":
                get:
                  responses:
                    200:
                      content:
                        "application/x.zalando.contract+json;v=123": {}
        """.trimIndent()
        )
        val result = rule.validate(context)
        assertThat(result).hasSameElementsAs(
            listOf(
                v("/paths/~1path1/get/responses/200/content/application~1vnd.api+json"),
                v("/paths/~1path2/get/responses/200/content/application~1x.zalando.contract+json")
            )
        )
    }

    @Test
    fun `invalid shared components cause violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.0
            components:
              requestBodies:
                NamedRequest:
                  content:
                    "application/invalid": {}
              responses:
                NamedResponse:
                  description: description
                  content:
                    "application/invalid": {}
            """.trimIndent())

        val result = rule.validate(context)
        assertThat(result).hasSameElementsAs(
            listOf(
                v("/components/requestBodies/NamedRequest/content/application~1invalid"),
                v("/components/responses/NamedResponse/content/application~1invalid")
            )
        )
    }

    @Test
    fun `the SPP API generates violations`() {
        val context = DefaultContextFactory().getSwaggerContext(loadFileFromClasspath("fixtures/api_spp.json"))
        val result = rule.validate(context)
        assertThat(result).hasSameElementsAs(
            listOf(
                // --- consumes ---
                v("/paths/~1products~1{product_id}/patch/consumes"),
                v("/paths/~1product-put-requests~1{product_path}/post/consumes"),
                // --- produces ---
                v("/paths/~1products/get/responses/200"),
                v("/paths/~1products~1{product_id}/get/responses/200"),
                v("/paths/~1products~1{product_id}~1children/get/responses/200"),
                v("/paths/~1products~1{product_id}~1updates~1{update_id}/get/responses/200"),
                v("/paths/~1request-groups~1{request_group_id}~1updates/get/responses/200")
            )
        )
    }

    @Test
    fun `the SPA API generates no violations`() {
        val context = DefaultContextFactory().getSwaggerContext(loadFileFromClasspath("fixtures/api_spa.yaml"))
        assertThat(rule.validate(context)).isEmpty()
    }

    private val rule = MediaTypesRule()

    private fun v(pointer: String) = Violation(
        description = "Custom media types should only be used for versioning",
        pointer = pointer.toJsonPointer()
    )
}
