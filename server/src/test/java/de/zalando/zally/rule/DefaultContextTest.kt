package de.zalando.zally.rule

import de.zalando.zally.util.ResourceUtil.resourceToString
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DefaultContextTest {
    @Test
    fun createSwaggerContextFromSwaggerJson() {
        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
        val context = DefaultContext.createSwaggerContext(content)
        assertThat(context).isNotNull
        assertThat(context?.api).isInstanceOf(OpenAPI::class.java)
        assertThat(context?.violation("", context.api.info.title)?.pointer).hasToString("/info/title")
    }

    @Test
    fun createOpenApiContextFromSwaggerJson() {
        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
        val context = DefaultContext.createOpenApiContext(content)
        assertThat(context).isNull()
    }

    @Test
    fun createOpenApiContextFromOpenApiJson() {
        val content = resourceToString("fixtures/openapi3_petstore_expanded.json")
        val context = DefaultContext.createOpenApiContext(content)
        assertThat(context).isNotNull
        assertThat(context?.api).isInstanceOf(OpenAPI::class.java)
        assertThat(context?.violation("", context.api.info.title)?.pointer).hasToString("/info/title")
    }

    @Test
    fun `should recognize the used OpenAPI 2 (aka Swagger)`() {
        val openapi3Context = DefaultContext.createSwaggerContext("""
        swagger: '2.0'
        info:
          version: 1.0.0
          title: Pets API
        paths: {}
        """.trimIndent())!!

        assertThat(openapi3Context.isOpenAPI3()).isFalse()
    }

    @Test
    fun `should recognize the used OpenAPI 3`() {
        val openapi3Context = DefaultContext.createOpenApiContext("""
        openapi: 3.0.1
        info:
          title: Pets API
          version: 1.0.0
        paths: {}
        """.trimIndent())!!

        assertThat(openapi3Context.isOpenAPI3()).isTrue()
    }
}
