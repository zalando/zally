package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.rule.Context
import de.zalando.zally.util.PatternUtil.isApplicationJsonOrProblemJson
import de.zalando.zally.util.PatternUtil.isCustomMediaTypeWithVersioning
import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import io.swagger.models.Operation as v2Operation
import io.swagger.models.Path as v2Path

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
        val context = Context(OpenAPI())
        assertThat(rule.validate(context)).isEmpty()
    }

    @Test
    fun `versioned custom media type causes no violation`() {
        val context = contextWithMediaTypes(
            "/shipment-order/{shipment_order_id}" to listOf(
                "application/x.zalando.contract+json;v=123",
                "application/vnd.api+json;version=3"))
        assertThat(rule.validate(context)).isEmpty()
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
                    path to v2Path().apply {
                        this["get"] = v2Operation().apply { produces = types }
                    }
                }
                .toMap()
        }

    private fun contextWithMediaTypes(vararg pathToMedia: Pair<String, List<String>>): Context =
        Context(OpenAPI().apply {
            paths = pathToMedia.fold(Paths()) { paths, (path, types) ->
                paths.addPathItem(path, PathItem().apply {
                    get = Operation().apply {
                        responses = ApiResponses().apply {
                            addApiResponse("200", ApiResponse().apply {
                                content = types.fold(Content()) { content, type ->
                                    content.addMediaType(type, MediaType())
                                }
                            })
                        }
                    }
                })
            }
        })

}
