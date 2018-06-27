package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.rule.Context
import de.zalando.zally.util.PatternUtil.isApplicationJsonOrProblemJson
import de.zalando.zally.util.PatternUtil.isCustomMediaTypeWithVersioning
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
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

    // todo #714: Delete this when conversion to `Context` is over.
    @Test
    fun `DEPRECATED empty specification causes no violation`() {
        assertThat(rule.validate(Swagger())).isNull()
    }

    @Test
    fun `empty specification causes no violation`() {
        val context = Context(OpenAPI())
        assertThat(rule.validate(context)).isEmpty()
    }

    @Test
    fun `versioned custom media type causes no violation`() {
        val swagger = swaggerWithMediaTypes(
            "/shipment-order/{shipment_order_id}" to listOf(
                "application/x.zalando.contract+json;v=123",
                "application/vnd.api+json;version=3"))
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun `custom media type without versioning causes violation`() {
        val path = "/shipment-order/{shipment_order_id}"
        val swagger = swaggerWithMediaTypes(path to listOf("application/json", "application/vnd.api+json"))
        assertThat(rule.validate(swagger)!!.paths).hasSameElementsAs(listOf("$path GET"))
    }

    @Test
    fun `only some of multiple paths without versioning causes violation`() {
        val swagger = swaggerWithMediaTypes(
            "/path1" to listOf("application/json", "application/vnd.api+json"),
            "/path2" to listOf("application/x.zalando.contract+json"),
            "/path3" to listOf("application/x.zalando.contract+json;v=123")
        )
        val result = rule.validate(swagger)!!
        println(result)
        assertThat(result.paths).hasSameElementsAs(listOf(
            "/path1 GET",
            "/path2 GET"
        ))
    }

    @Test
    fun negativeCaseSpp() {
        val swagger = getFixture("api_spp.json")
        val result = rule.validate(swagger)!!
        assertThat(result.paths).hasSameElementsAs(listOf(
            "/products GET",
            "/products/{product_id} GET",
            "/products/{product_id} PATCH",
            "/products/{product_id}/children GET",
            "/products/{product_id}/updates/{update_id} GET",
            "/product-put-requests/{product_path} POST",
            "/request-groups/{request_group_id}/updates GET"))
    }

    @Test
    fun positiveCaseSpa() {
        val swagger = getFixture("api_spa.yaml")
        assertThat(rule.validate(swagger)).isNull()
    }

    private val rule = MediaTypesRule()

    private fun swaggerWithMediaTypes(vararg pathToMedia: Pair<String, List<String>>): Swagger =
        Swagger().apply {
            paths = pathToMedia
                .map { (path, types) ->
                    path to Path().apply {
                        this["get"] = Operation().apply { produces = types }
                    }
                }
                .toMap()
        }

}
