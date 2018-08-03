package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.getContextFromFixture
import de.zalando.zally.rule.DefaultContext
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil.isApplicationJsonOrProblemJson
import de.zalando.zally.util.PatternUtil.isCustomMediaTypeWithVersioning
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class MediaTypesRuleTest {

    @Test
    fun `isApplicationJsonOrProblemJson for valid input`() {
        assertThat(isApplicationJsonOrProblemJson("application/json")).isTrue()
        assertThat(isApplicationJsonOrProblemJson("application/problem+json")).isTrue()
    }

    @Test
    fun `isApplicationJsonOrProblemJson for invalid input`() {
        assertThat(isApplicationJsonOrProblemJson("application/vnd.api+json")).isFalse()
        assertThat(isApplicationJsonOrProblemJson("application/x.zalando.contract+json")).isFalse()
    }

    @Test
    fun `isCustomMediaTypeWithVersioning for valid input`() {
        assertThat(isCustomMediaTypeWithVersioning("application/vnd.api+json;v=12")).isTrue()
        assertThat(isCustomMediaTypeWithVersioning("application/x.zalando.contract+json;v=34")).isTrue()
        assertThat(isCustomMediaTypeWithVersioning("application/vnd.api+json;version=123")).isTrue()
        assertThat(isCustomMediaTypeWithVersioning("application/x.zalando.contract+json;version=345")).isTrue()
    }

    @Test
    fun `isCustomMediaTypeWithVersioning for invalid input`() {
        assertThat(isCustomMediaTypeWithVersioning("application/vnd.api+json")).isFalse()
        assertThat(isCustomMediaTypeWithVersioning("application/x.zalando.contract+json")).isFalse()
        assertThat(isCustomMediaTypeWithVersioning("application/vnd.api+json;ver=1")).isFalse()
        assertThat(isCustomMediaTypeWithVersioning("application/x.zalando.contract+json;v:1")).isFalse()
        assertThat(isCustomMediaTypeWithVersioning("application/vnd.api+json;version=")).isFalse()
        assertThat(isCustomMediaTypeWithVersioning("application/x.zalando.contract+json;")).isFalse()
    }

    @Test
    fun `empty specification causes no violation`() {
        val context = DefaultContext("", OpenAPI())
        assertThat(rule.validate(context)).isEmpty()
    }

    @Test
    fun `versioned custom media type causes no violation`() {
        @Language("YAML")
        val context = DefaultContext.createOpenApiContext("""
            openapi: 3.0.0
            paths:
              "/shipment-order/{shipment_order_id}":
                get:
                  responses:
                    200:
                      content:
                        "application/x.zalando.contract+json;v=123": {}
                        "application/vnd.api+json;version=3": {}
        """.trimIndent())!!
        assertThat(rule.validate(context)).isEmpty()
    }

    @Test
    fun `custom media type without versioning causes violation`() {
        @Language("YAML")
        val context = DefaultContext.createOpenApiContext("""
            openapi: 3.0.0
            paths:
              "/shipment-order/{shipment_order_id}":
                get:
                  responses:
                    200:
                      content:
                        "application/json": {}
                        "application/vnd.api+json": {}
        """.trimIndent())!!
        assertThat(rule.validate(context)).hasSameElementsAs(listOf(
            v("/paths/~1shipment-order~1{shipment_order_id}/get/responses/200/content/application~1vnd.api+json")
        ))
    }

    @Test
    fun `only some of multiple paths without versioning causes violation`() {
        @Language("YAML")
        val context = DefaultContext.createOpenApiContext("""
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
        """.trimIndent())!!
        val result = rule.validate(context)
        assertThat(result).hasSameElementsAs(listOf(
            v("/paths/~1path1/get/responses/200/content/application~1vnd.api+json"),
            v("/paths/~1path2/get/responses/200/content/application~1x.zalando.contract+json")
        ))
    }

    @Test
    fun `the SPP API generates violations`() {
        val context = getContextFromFixture("api_spp.json")
        val result = rule.validate(context)
        assertThat(result).hasSameElementsAs(listOf(
            // --- consumes ---
            v("/paths/~1products~1{product_id}/patch/consumes"),
            v("/paths/~1product-put-requests~1{product_path}/post/consumes"),
            // --- produces ---
            v("/paths/~1products/get/responses/200"),
            v("/paths/~1products~1{product_id}/get/responses/200"),
            v("/paths/~1products~1{product_id}~1children/get/responses/200"),
            v("/paths/~1products~1{product_id}~1updates~1{update_id}/get/responses/200"),
            v("/paths/~1request-groups~1{request_group_id}~1updates/get/responses/200")
        ))
    }

    @Test
    fun `the SPA API generates no violations`() {
        val context = getContextFromFixture("api_spa.yaml")
        assertThat(rule.validate(context)).isEmpty()
    }

    private val rule = MediaTypesRule()

    private fun v(pointer: String) = Violation(
        description = "Custom media types should only be used for versioning",
        pointer = JsonPointer.compile(pointer)
    )
}
