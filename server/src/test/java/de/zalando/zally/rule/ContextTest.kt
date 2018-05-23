package de.zalando.zally.rule

import de.zalando.zally.util.ResourceUtil.resourceToString
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ContextTest {
    @Test
    fun createSwaggerContextFromSwaggerJson() {
        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
        val context = Context.createSwaggerContext(content)
        assertThat(context).isNotNull
        assertThat(context?.api).isInstanceOf(OpenAPI::class.java)
        assertThat(context?.pointerForValue(context.api.info.title)).isEqualTo("#/info/title")
    }

    @Test
    fun createOpenApiContextFromSwaggerJson() {
        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
        val context = Context.createOpenApiContext(content)
        assertThat(context).isNull()
    }

    @Test
    fun createOpenApiContextFromOpenApiJson() {
        val content = resourceToString("fixtures/openapi3_petstore_expanded.json")
        val context = Context.createOpenApiContext(content)
        assertThat(context).isNotNull
        assertThat(context?.api).isInstanceOf(OpenAPI::class.java)
        assertThat(context?.pointerForValue(context.api.info.title)).isEqualTo("#/info/title")
    }
}
